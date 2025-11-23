import React, { useEffect, useState, useCallback } from "react";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import "./HomeActivityTicker.css";

const MAX_TEXT_LENGTH = 80;

const truncate = (text) => {
  if (!text) return "";
  if (text.length <= MAX_TEXT_LENGTH) return text;
  return `${text.slice(0, MAX_TEXT_LENGTH - 1)}…`;
};

const HomeActivityTicker = ({ maxItems = 6 }) => {
  const { t } = useTranslation();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchTicker = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get("/activity-stream/", {
        params: { limit: maxItems },
      });
      const normalized = (data?.orderedItems || []).slice(0, maxItems);
      setItems(normalized);
    } catch (error) {
      console.error("Failed to load home activity ticker", error);
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [maxItems]);

  useEffect(() => {
    fetchTicker();
  }, [fetchTicker]);

  if (loading || items.length === 0) {
    return (
      <div className="activity-ticker">
        <span className="activity-ticker__state">
          {loading ? t("common.loading") : t("activity.empty")}
        </span>
      </div>
    );
  }

  return (
    <div className="activity-ticker">
      {items.map((item) => (
        <div key={item.id} className="activity-ticker__item">
          <span className="activity-ticker__type">{item.type}</span>
          <span className="activity-ticker__summary">
            {truncate(item.summary || t("activity.unknownEvent"))}
          </span>
        </div>
      ))}
      <button
        className="activity-ticker__refresh"
        type="button"
        onClick={fetchTicker}
      >
        {t("activity.refresh")}
      </button>
    </div>
  );
};

export default HomeActivityTicker;

