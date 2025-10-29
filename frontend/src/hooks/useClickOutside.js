import { useEffect, useRef } from 'react';

const useClickOutside = (onClickOutside) => {
    const ref = useRef(null);
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (ref.current && !ref.current.contains(event.target)) {
                onClickOutside();
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [onClickOutside]);
    return ref;
};

export default useClickOutside;
