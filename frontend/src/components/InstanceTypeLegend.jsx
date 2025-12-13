import PropTypes from 'prop-types';
import { useTranslation } from '../contexts/TranslationContext';
import { getGroupById } from '../config/instanceTypes';

const InstanceTypeLegend = ({ 
  visibleTypes,
  isVisible,
  onToggle
}) => {
  const { t } = useTranslation();

  return (
    <div style={{ marginTop: '16px' }}>
      {/* Toggle Button */}
      <button
        onClick={onToggle}
        style={{
          width: '100%',
          padding: '8px 12px',
          backgroundColor: 'var(--color-accent)',
          color: 'var(--color-white)',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '13px',
          fontWeight: '600'
        }}
      >
        {isVisible ? t('instanceTypes.hideLegend') : t('instanceTypes.showLegend')}
      </button>

      {/* Legend Content */}
      {isVisible && (
        <div style={{
          marginTop: '8px',
          border: '1px solid var(--color-gray-300)',
          borderRadius: '8px',
          backgroundColor: 'var(--color-white)',
          padding: '12px'
        }}>
          <div style={{
            fontSize: '14px',
            fontWeight: '600',
            marginBottom: '12px',
            color: 'var(--color-text)'
          }}>
            {t('instanceTypes.legend')}
          </div>

          {visibleTypes.map(type => {
            const group = getGroupById(type.group_id);
            
            return (
              <div
                key={type.group_id}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  marginBottom: '8px'
                }}
              >
                {/* Color Dot */}
                {group && (
                  <div
                    style={{
                      width: '14px',
                      height: '14px',
                      borderRadius: '50%',
                      backgroundColor: group.color,
                      marginRight: '8px',
                      border: '2px solid var(--color-gray-300)',
                      flexShrink: 0
                    }}
                  />
                )}
                
                {/* Icon + Label */}
                <span style={{ fontSize: '12px', color: 'var(--color-text)' }}>
                  {group && group.icon && `${group.icon} `}
                  {group ? t(`instanceTypes.groups.${group.id}`, { defaultValue: group.label }) : type.group_label}
                </span>
              </div>
            );
          })}

          {/* Default Color */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            marginTop: '12px',
            paddingTop: '8px',
            borderTop: '1px solid var(--color-gray-200)'
          }}>
            <div
              style={{
                width: '14px',
                height: '14px',
                borderRadius: '50%',
                backgroundColor: 'var(--color-success)',
                marginRight: '8px',
                border: '2px solid var(--color-gray-300)',
                flexShrink: 0
              }}
            />
            <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)', fontStyle: 'italic' }}>
              {t('instanceTypes.noInstanceType')}
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

InstanceTypeLegend.propTypes = {
  visibleTypes: PropTypes.array.isRequired,
  isVisible: PropTypes.bool.isRequired,
  onToggle: PropTypes.func.isRequired
};

export default InstanceTypeLegend;
