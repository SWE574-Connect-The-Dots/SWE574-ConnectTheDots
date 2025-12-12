import { useState } from 'react';
import PropTypes from 'prop-types';
import { useTranslation } from '../contexts/TranslationContext';
import { getGroupById } from '../config/instanceTypes';

const InstanceTypeFilter = ({ 
  instanceTypes,
  selectedTypes,
  onToggleType,
  onSelectAll,
  onDeselectAll
}) => {
  const { t } = useTranslation();
  const [isExpanded, setIsExpanded] = useState(true);

  return (
    <div style={{
      border: '1px solid var(--color-gray-300)',
      borderRadius: '8px',
      backgroundColor: 'var(--color-white)',
      overflow: 'hidden'
    }}>
      {/* Header */}
      <div
        onClick={() => setIsExpanded(!isExpanded)}
        style={{
          padding: '12px 16px',
          backgroundColor: 'var(--color-bg)',
          borderBottom: isExpanded ? '1px solid var(--color-gray-300)' : 'none',
          cursor: 'pointer',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}
      >
        <strong style={{ color: 'var(--color-text)' }}>
          {t('instanceTypes.filterByType')}
        </strong>
        <span>{isExpanded ? '▼' : '▲'}</span>
      </div>

      {/* Content */}
      {isExpanded && (
        <div style={{ padding: '12px' }}>
          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
            <button
              onClick={onSelectAll}
              style={{
                flex: 1,
                padding: '6px 12px',
                fontSize: '12px',
                backgroundColor: 'var(--color-success)',
                color: 'var(--color-white)',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              {t('instanceTypes.selectAll')}
            </button>
            <button
              onClick={onDeselectAll}
              style={{
                flex: 1,
                padding: '6px 12px',
                fontSize: '12px',
                backgroundColor: 'var(--color-gray-400)',
                color: 'var(--color-white)',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              {t('instanceTypes.deselectAll')}
            </button>
          </div>

          {/* Instance Type Group List */}
          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {instanceTypes.map(type => {
              const group = getGroupById(type.group_id);
              const isSelected = selectedTypes.has(type.group_id);

              return (
                <div
                  key={type.group_id}
                  onClick={() => onToggleType(type.group_id)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    padding: '8px',
                    marginBottom: '4px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    backgroundColor: isSelected 
                      ? 'var(--color-item-bg)' 
                      : 'transparent',
                    transition: 'background-color 0.2s'
                  }}
                >
                  <input
                    type="checkbox"
                    checked={isSelected}
                    onChange={() => onToggleType(type.group_id)}
                    onClick={(e) => e.stopPropagation()}
                    style={{ marginRight: '8px' }}
                  />
                  
                  {/* Color Dot */}
                  {group && (
                    <div
                      style={{
                        width: '16px',
                        height: '16px',
                        borderRadius: '50%',
                        backgroundColor: group.color,
                        marginRight: '8px',
                        border: '2px solid var(--color-gray-300)',
                        flexShrink: 0
                      }}
                    />
                  )}
                  
                  {/* Icon */}
                  {group && group.icon && (
                    <span style={{ marginRight: '6px', fontSize: '14px' }}>
                      {group.icon}
                    </span>
                  )}
                  
                  {/* Label */}
                  <span style={{
                    flex: 1,
                    fontSize: '13px',
                    color: 'var(--color-text)'
                  }}>
                    {group ? t(`instanceTypes.groups.${group.id}`, { defaultValue: group.label }) : type.group_label}
                  </span>
                  
                  {/* Count Badge */}
                  <span style={{
                    backgroundColor: 'var(--color-gray-200)',
                    color: 'var(--color-text-secondary)',
                    padding: '2px 8px',
                    borderRadius: '12px',
                    fontSize: '11px',
                    fontWeight: '600'
                  }}>
                    {type.count}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

InstanceTypeFilter.propTypes = {
  instanceTypes: PropTypes.arrayOf(
    PropTypes.shape({
      group_id: PropTypes.string.isRequired,
      group_label: PropTypes.string.isRequired,
      count: PropTypes.number.isRequired
    })
  ).isRequired,
  selectedTypes: PropTypes.instanceOf(Set).isRequired,
  onToggleType: PropTypes.func.isRequired,
  onSelectAll: PropTypes.func.isRequired,
  onDeselectAll: PropTypes.func.isRequired
};

export default InstanceTypeFilter;
