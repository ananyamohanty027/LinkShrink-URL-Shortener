import React, { useState } from 'react';
import { Link, ExternalLink, Copy, CheckCircle } from 'lucide-react';
// 1. IMPORT THE QR CODE LIBRARY
import { QRCodeCanvas } from 'qrcode.react';

const App = () => {
  const [url, setUrl] = useState('');
  const [shortUrl, setShortUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState('');

  // -------------------------------------------------------------------------
  // CONFIGURATION: Dynamic API URL
  // -------------------------------------------------------------------------
  const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setShortUrl('');
    setCopied(false);

    try {
      console.log(`Sending request to: ${API_BASE_URL}/api/v1/shorten`);
      
      const response = await fetch(`${API_BASE_URL}/api/v1/shorten`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ originalUrl: url }),
      });

      if (!response.ok) {
         // Check if it's the rate limit error
         if (response.status === 429) {
            throw new Error('Rate limit exceeded! You are going too fast.');
         }
         throw new Error('Failed to shorten URL. Is the backend running?');
      }

      const data = await response.json();
      setShortUrl(data.shortUrl);
    } catch (err) {
      console.error(err);
      setError(err.message || 'Server error. Ensure backend is deployed and running.');
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(shortUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    // MAIN CONTAINER
    <div className="w-full min-h-screen bg-slate-900 text-white flex items-center justify-center p-4">

      {/* THE APP BOX */}
      <div className="max-w-md w-full bg-slate-800 p-8 rounded-2xl shadow-2xl border border-slate-700">

        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent mb-2">
            LinkShrink
          </h1>
          <p className="text-slate-400">Enterprise-grade URL Shortener</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1">
              Paste your long URL
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Link className="h-5 w-5 text-slate-500" />
              </div>
              <input
                type="url"
                required
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                className="block w-full pl-10 pr-3 py-3 border border-slate-600 rounded-lg leading-5 bg-slate-700 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm transition duration-150 ease-in-out"
                placeholder="https://example.com/very/long/url..."
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className={`w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors ${
              loading ? 'opacity-75 cursor-not-allowed' : ''
            }`}
          >
            {loading ? (
              <span className="flex items-center">
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Processing...
              </span>
            ) : (
              'Shorten URL'
            )}
          </button>
        </form>

        {error && (
          <div className="mt-4 p-3 bg-red-900/50 border border-red-500/50 rounded-lg text-red-200 text-sm text-center">
            {error}
          </div>
        )}

        {shortUrl && (
          <div className="mt-8 p-4 bg-slate-700/50 rounded-xl border border-slate-600 animate-fade-in-up">
            <p className="text-xs text-slate-400 uppercase tracking-wider font-semibold mb-2">
              Your Shortened Link
            </p>
            <div className="flex items-center justify-between bg-slate-900 p-3 rounded-lg border border-slate-700 mb-4">
              <a
                href={shortUrl}
                target="_blank"
                rel="noreferrer"
                className="text-blue-400 hover:text-blue-300 truncate mr-3 font-mono"
              >
                {shortUrl}
              </a>
              <div className="flex space-x-2">
                <a
                  href={shortUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="p-2 text-slate-400 hover:text-white transition-colors"
                >
                  <ExternalLink className="h-5 w-5" />
                </a>
                <button
                  onClick={copyToClipboard}
                  className="p-2 text-slate-400 hover:text-white transition-colors"
                >
                  {copied ? <CheckCircle className="h-5 w-5 text-green-500" /> : <Copy className="h-5 w-5" />}
                </button>
              </div>
            </div>

            {/* --- NEW QR CODE SECTION --- */}
            <div className="flex flex-col items-center justify-center border-t border-slate-600 pt-4">
                <p className="text-sm text-slate-400 mb-3">Scan to share instantly</p>
                <div className="p-3 bg-white rounded-xl shadow-lg">
                    <QRCodeCanvas 
                        value={shortUrl} 
                        size={128}
                        level={"H"}
                    />
                </div>
            </div>

            <div className="mt-4 text-center">
              <span className="text-xs text-green-400 bg-green-900/30 px-2 py-1 rounded-full border border-green-800">
                ⚡ Write-Around Cache Active
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default App;
