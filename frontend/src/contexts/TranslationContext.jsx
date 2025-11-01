import { createContext, useContext } from 'react';
import { useTranslation as useI18next } from 'react-i18next';

const TranslationContext = createContext();

/**
 * Translation Provider that makes 't' function available to all children
 * without needing to import useTranslation in every component
 */
export function TranslationProvider({ children }) {
  const { t, i18n } = useI18next();
  
  const value = {
    t,
    i18n,
    changeLanguage: (lng) => i18n.changeLanguage(lng),
    currentLanguage: i18n.language,
  };

  return (
    <TranslationContext.Provider value={value}>
      {children}
    </TranslationContext.Provider>
  );
}

/**
 * Hook to use translations anywhere in the app
 * Usage: const { t } = useTranslation();
 */
export function useTranslation() {
  const context = useContext(TranslationContext);
  if (!context) {
    throw new Error('useTranslation must be used within a TranslationProvider');
  }
  return context;
}

