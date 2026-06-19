import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPwd, setShowPwd] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const role = await login(username, password);
      navigate(role === 'ADMIN' ? '/admin' : '/annotator');
    } catch (err) {
      if (err.response?.status === 401) {
        const msg = err.response?.data?.error;
        if (msg === 'invalid_credentials') {
          setError('Invalid username or password');
        } else if (msg === 'invalid_refresh_token') {
          setError('Session expired. Please login again.');
        } else {
          setError(msg || 'Invalid username or password');
        }
      } else if (err.code === 'ERR_NETWORK') {
        setError('Cannot reach server. Is the backend running?');
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'radial-gradient(circle at 50% 30%, #22c55e 0%, #15803d 55%, #14532d 100%)',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
      position: 'relative',
      overflow: 'hidden',
    }}>
      <div style={{
        position: 'absolute',
        width: '260px', height: '260px',
        top: '-60px', left: '-80px',
        background: 'rgba(255,255,255,0.08)',
        borderRadius: '50%',
      }} />
      <div style={{
        position: 'absolute',
        width: '320px', height: '320px',
        bottom: '-100px', right: '-100px',
        background: 'rgba(255,255,255,0.08)',
        borderRadius: '50%',
      }} />
      <div style={{
        position: 'absolute',
        width: '180px', height: '180px',
        top: '60%', left: '5%',
        background: 'rgba(255,255,255,0.04)',
        borderRadius: '50%',
      }} />

      <div style={{
        position: 'relative',
        width: '960px',
        maxWidth: '92vw',
        aspectRatio: '960/545',
        background: 'rgba(255,255,255,0.04)',
        borderRadius: '24px',
        boxShadow: '0 30px 80px rgba(0,0,10,0.45)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}>
        <form onSubmit={handleSubmit} style={{
          width: '290px',
          background: 'rgba(255,255,255,0.16)',
          backdropFilter: 'blur(18px)',
          WebkitBackdropFilter: 'blur(18px)',
          border: '1px solid rgba(255,255,255,0.25)',
          borderRadius: '16px',
          padding: '28px 24px 20px',
          boxShadow: '0 10px 40px rgba(0,0,30,0.35)',
          zIndex: 2,
        }}>
          <div style={{
            textAlign: 'center',
            color: '#fff',
            fontWeight: 700,
            fontSize: '15px',
            marginBottom: '18px',
          }}>
            NLP Platform
          </div>

          <h1 style={{
            color: '#fff',
            fontSize: '19px',
            fontWeight: 700,
            margin: '0 0 16px',
          }}>
            Login
          </h1>

          {error && (
            <p style={{
              color: '#fca5a5',
              fontSize: '11px',
              marginBottom: '12px',
              textAlign: 'center',
            }}>
              {error}
            </p>
          )}

          <div style={{ marginBottom: '12px' }}>
            <label style={{
              display: 'block',
              color: '#fff',
              fontSize: '11px',
              fontWeight: 600,
              marginBottom: '5px',
            }}>
              Username
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="username"
              required
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: '8px',
                border: 'none',
                outline: 'none',
                fontSize: '12px',
                background: '#fff',
                color: '#1a1a1a',
                boxSizing: 'border-box',
              }}
            />
          </div>

          <div style={{ marginBottom: '12px' }}>
            <label style={{
              display: 'block',
              color: '#fff',
              fontSize: '11px',
              fontWeight: 600,
              marginBottom: '5px',
            }}>
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <input
                type={showPwd ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password"
                required
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '8px',
                  border: 'none',
                  outline: 'none',
                  fontSize: '12px',
                  background: '#fff',
                  color: '#1a1a1a',
                  boxSizing: 'border-box',
                  paddingRight: '30px',
                }}
              />
              <span
                onClick={() => setShowPwd(!showPwd)}
                style={{
                  position: 'absolute',
                  right: '10px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  cursor: 'pointer',
                  fontSize: '13px',
                  color: '#9aa0a6',
                  userSelect: 'none',
                }}
              >
                {showPwd ? '\u{1F441}' : '\u{1F441}'}
              </span>
            </div>
          </div>

          <div style={{
            textAlign: 'right',
            margin: '-2px 0 14px',
          }}>
            <a href="#" onClick={(e) => e.preventDefault()} style={{
              color: '#fff',
              fontSize: '11px',
              textDecoration: 'none',
              opacity: 0.9,
            }}>
              Forgot Password?
            </a>
          </div>

          <button
            type="submit"
            style={{
              width: '100%',
              padding: '10px',
              border: 'none',
              borderRadius: '8px',
              background: '#0b2a55',
              color: '#fff',
              fontWeight: 700,
              fontSize: '13px',
              cursor: 'pointer',
            }}
            onMouseOver={(e) => e.currentTarget.style.background = '#0d3567'}
            onMouseOut={(e) => e.currentTarget.style.background = '#0b2a55'}
          >
            Sign in
          </button>
        </form>
      </div>
    </div>
  );
}
