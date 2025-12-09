import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import AppRouter from './router/AppRouter';
import './App.css';

const App: React.FC = () => {
    return (
        <Router>
            <ThemeProvider>
                <AuthProvider>
                    <AppRouter />
                </AuthProvider>
            </ThemeProvider>
        </Router>
    );
};

export default App;