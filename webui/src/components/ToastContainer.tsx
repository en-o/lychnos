import React, { useState, useCallback } from 'react';
import Toast, { type ToastType } from './Toast';

interface ToastItem {
  id: number;
  message: string;
  type: ToastType;
}

let toastCounter = 0;
let addToastCallback: ((message: string, type: ToastType) => void) | null = null;

export const toast = {
  success: (message: string) => {
    addToastCallback?.(message, 'success');
  },
  error: (message: string) => {
    addToastCallback?.(message, 'error');
  },
  warning: (message: string) => {
    addToastCallback?.(message, 'warning');
  },
  info: (message: string) => {
    addToastCallback?.(message, 'info');
  },
};

const ToastContainer: React.FC = () => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const addToast = useCallback((message: string, type: ToastType) => {
    const id = toastCounter++;
    setToasts((prev) => [...prev, { id, message, type }]);
  }, []);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  // 设置全局回调
  React.useEffect(() => {
    addToastCallback = addToast;
    return () => {
      addToastCallback = null;
    };
  }, [addToast]);

  return (
    <div className="fixed top-4 right-4 z-[9999] flex flex-col gap-2 max-w-md">
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          message={toast.message}
          type={toast.type}
          onClose={() => removeToast(toast.id)}
        />
      ))}

      <style>{`
        @keyframes slideIn {
          from {
            transform: translateX(100%);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }
        .animate-slideIn {
          animation: slideIn 0.3s ease-out;
        }
      `}</style>
    </div>
  );
};

export default ToastContainer;
