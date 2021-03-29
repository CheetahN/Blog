DELETE FROM global_settings;
INSERT INTO global_settings (id, code, name, value) VALUES(1, 'MULTIUSER_MODE', 'Многопользовательский режим', 'YES');
INSERT INTO global_settings (id, code, name, value) VALUES(2, 'POST_PREMODERATION', 'Премодерация постов', 'NO');
INSERT INTO global_settings (id, code, name, value) VALUES(3, 'STATISTICS_IS_PUBLIC', 'Показывать всем статистику блога', 'NO');