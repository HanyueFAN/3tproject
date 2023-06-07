CREATE DATABASE t3_database;

USE t3_database;

CREATE TABLE tourist_attractions (
                                     id INT PRIMARY KEY,
                                     name VARCHAR(100),
                                     description TEXT,
                                     location VARCHAR(100),
                                     period VARCHAR(100),
                                     type VARCHAR(100),
                                     latitude DOUBLE,
                                     longitude DOUBLE
);

show databases;

INSERT INTO tourist_attractions (id, name, description, location, period, type, latitude, longitude)
VALUES
    (1, 'Eiffel Tower', 'Iconic tower in Paris', 'Paris', 'Modern', 'Landmark', 48.8584, 2.2945),
    (2, 'Louvre Museum', 'World-renowned art museum', 'Paris', 'Renaissance', 'Museum', 48.8606, 2.3376),
    (3, 'Mont Saint-Michel', 'Medieval abbey on a rocky island', 'Normandy', 'Medieval', 'Landmark', 48.6361, -1.5114);


INSERT INTO tourist_attractions (id, name, description, location, period, type, latitude, longitude)
VALUES
    (4, 'Colosseum', 'Ancient amphitheater in Rome', 'Rome', 'Ancient', 'Landmark', 41.8902, 12.4922),
    (5, 'Machu Picchu', 'Incan citadel in the Andes', 'Peru', 'Ancient', 'Archaeological Site', -13.1631, -72.5450);

INSERT INTO tourist_attractions (id, name, description, location, period, type, latitude, longitude)
VALUES
    (6, 'Palace of Versailles', 'Opulent palace with extensive gardens', 'Versailles', 'Baroque', 'Palace', 48.8049, 2.1204),
    (7, 'Côte d\'Azur', 'Stunning coastline and glamorous resorts', 'French Riviera', 'Modern', 'Beach', 43.7102, 7.2620),
    (8, 'Château de Chambord', 'Magnificent Renaissance castle', 'Loir-et-Cher', 'Renaissance', 'Castle', 47.6166, 1.5114),
    (9, 'D-Day Beaches', 'Historic World War II landing sites', 'Normandy', 'Modern', 'Historical', 49.3464, -0.8899),
    (10, 'Provence Lavender Fields', 'Vast fields of fragrant lavender', 'Provence', 'Modern', 'Natural', 43.9296, 5.1043);

INSERT INTO tourist_attractions (id, name, description, location, period, type, latitude, longitude)
VALUES
    (11, 'Notre-Dame Cathedral', 'Gothic masterpiece and iconic cathedral', 'Paris', 'Gothic', 'Religious', 48.8530, 2.3499),
    (12, 'Palais Garnier', 'Opulent opera house with ornate architecture', 'Paris', 'Modern', 'Opera House', 48.8719, 2.3316),
    (13, 'Sainte-Chapelle', 'Stunning medieval chapel with beautiful stained glass', 'Paris', 'Medieval', 'Religious', 48.8550, 2.3458),
    (14, 'Musée d\'Orsay', 'Art museum housed in a former railway station', 'Paris', 'Modern', 'Museum', 48.8599, 2.3266),
    (15, 'Champs-Élysées', 'Famous avenue lined with shops and landmarks', 'Paris', 'Modern', 'Street', 48.8698, 2.3077);

INSERT INTO tourist_attractions (id, name, description, location, period, type, latitude, longitude)
VALUES
    (16, 'Musée du Louvre', 'World\'s largest art museum and historic monument', 'Paris', 'Modern', 'Museum', 48.8606, 2.3376),
    (17, 'Musée d\'Orsay', 'Art museum housed in a former railway station', 'Paris', 'Modern', 'Museum', 48.8599, 2.3266),
    (18, 'Musée de l\'Orangerie', 'Art museum showcasing impressionist and post-impressionist masterpieces', 'Paris', 'Modern', 'Museum', 48.8638, 2.3220),
    (19, 'Musée Rodin', 'Museum dedicated to the works of sculptor Auguste Rodin', 'Paris', 'Modern', 'Museum', 48.8551, 2.3156),
    (20, 'Musée de l\'Armée', 'Military museum located in the historic Hôtel des Invalides', 'Paris', 'Modern', 'Museum', 48.8566, 2.3129);

INSERT INTO tourist_attractions (id, name, description, location, period, type, latitude, longitude)
VALUES
    (21, 'Centre Pompidou', 'Modern and contemporary art museum with a distinctive architectural design', 'Paris', 'Modern', 'Museum', 48.8606, 2.3522),
    (22, 'Musée de l\'Art Moderne de Paris', 'Museum of modern and contemporary art located in the Palais de Tokyo', 'Paris', 'Modern', 'Museum', 48.8644, 2.3002),
    (23, 'Musée Picasso', 'Museum dedicated to the works of artist Pablo Picasso', 'Paris', 'Modern', 'Museum', 48.8599, 2.3625),
    (24, 'Musée de l\'Histoire de France', 'Museum tracing the history of France from the Middle Ages to the 20th century', 'Paris', 'Modern', 'Museum', 48.8599, 2.3704),
    (25, 'Musée Carnavalet', 'Museum showcasing the history of Paris through its collections of art and artifacts', 'Paris', 'Modern', 'Museum', 48.8576, 2.3636);

ALTER TABLE tourist_attractions ADD COLUMN visitDuration INT;

UPDATE tourist_attractions SET visitDuration = 1 WHERE id IN (1, 2, 3); -- Eiffel Tower, Louvre Museum, Mont Saint-Michel
UPDATE tourist_attractions SET visitDuration = 2 WHERE id IN (4, 5, 6, 7, 8, 9, 10); -- Colosseum, Machu Picchu, Palace of Versailles, Côte d'Azur, Château de Chambord, D-Day Beaches, Provence Lavender Fields
UPDATE tourist_attractions SET visitDuration = 3 WHERE id IN (11, 12, 13, 14, 15, 16, 17, 18, 19, 20); -- Notre-Dame Cathedral, Palais Garnier, Sainte-Chapelle, Musée d'Orsay, Champs-Élysées, Musée du Louvre, Musée de l'Orangerie, Musée Rodin, Musée de l'Armée
UPDATE tourist_attractions SET visitDuration = 4 WHERE id IN (21, 22, 23, 24, 25); -- Centre Pompidou, Musée de l'Art Moderne de Paris, Musée Picasso, Musée de l'Histoire de France, Musée Carnavalet

