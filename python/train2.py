import argparse
import json
import os
import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, f1_score
from torch.utils.data import DataLoader, TensorDataset

# Définition d'un réseau de neurones simple et rapide
class SimpleTextClassifier(nn.Module):
    def __init__(self, input_size, hidden_size, num_classes):
        super(SimpleTextClassifier, self).__init__()
        self.fc1 = nn.Linear(input_size, hidden_size)
        self.relu = nn.ReLU()
        self.fc2 = nn.Linear(hidden_size, num_classes)

    def forward(self, x):
        out = self.fc1(x)
        out = self.relu(out)
        out = self.fc2(out)
        return out

def main():
    # 1. Parsing des arguments
    parser = argparse.ArgumentParser()
    parser.add_argument('--dataset', type=str, required=True, help="Chemin vers le CSV")
    parser.add_argument('--config', type=str, required=True, help="Chemin vers config.json")
    args = parser.parse_args()

    # 2. Chargement de la config
    with open(args.config, 'r') as f:
        config = json.load(f)
    
    os.makedirs(config['output_dir'], exist_ok=True)

    print("Début du script de test rapide (PyTorch CPU/GPU)...")

    # 3. Préparation des données factices ou réelles
    # On gère l'erreur au cas où le fichier n'est pas bon
    try:
        df = pd.read_csv(args.dataset)
        texts = df['text'].tolist()
        labels = df['label'].tolist()
    except Exception as e:
        print("Erreur de lecture du CSV. Utilisation de données générées pour le test.")
        texts = ["je suis heureux"] * 50 + ["je suis triste"] * 50
        labels = [1] * 50 + [0] * 50

    num_classes = len(set(labels))

    # Vectorisation TF-IDF (très rapide)
    vectorizer = TfidfVectorizer(max_features=1000)
    X = vectorizer.fit_transform(texts).toarray()
    y = labels

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # Conversion en Tensors PyTorch
    train_dataset = TensorDataset(torch.tensor(X_train, dtype=torch.float32), torch.tensor(y_train, dtype=torch.long))
    train_loader = DataLoader(train_dataset, batch_size=config['batch_size'], shuffle=True)

    # 4. Initialisation du modèle
    model = SimpleTextClassifier(input_size=X_train.shape[1], hidden_size=config['hidden_size'], num_classes=num_classes)
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=config['learning_rate'])

    # 5. Boucle d'entraînement (Génération de l'historique pour les graphiques)
    history = []
    
    for epoch in range(config['num_epochs']):
        model.train()
        epoch_loss = 0
        
        for batch_x, batch_y in train_loader:
            optimizer.zero_grad()
            outputs = model(batch_x)
            loss = criterion(outputs, batch_y)
            loss.backward()
            optimizer.step()
            epoch_loss += loss.item()
            
        avg_loss = epoch_loss / len(train_loader)
        
        # Test rapide à chaque époque pour le graphique
        model.eval()
        with torch.no_grad():
            test_outputs = model(torch.tensor(X_test, dtype=torch.float32))
            _, predicted = torch.max(test_outputs.data, 1)
            acc = accuracy_score(y_test, predicted.numpy())
            
        print(f"Epoch [{epoch+1}/{config['num_epochs']}], Loss: {avg_loss:.4f}, Accuracy: {acc:.4f}")
        
        # On sauvegarde les données pour le graphique Chart.js du frontend !
        history.append({
            "epoch": epoch + 1,
            "loss": round(avg_loss, 4),
            "accuracy": round(acc, 4)
        })

    # 6. Évaluation Finale
    final_f1 = f1_score(y_test, predicted.numpy(), average='weighted')

    final_metrics = {
        "status": "success",
        "accuracy": history[-1]["accuracy"],
        "f1": round(final_f1, 4),
        "history": history, # C'est ce tableau que ton Chart.js va utiliser !
        "model_path": f"{config['output_dir']}/model.pt"
    }

    # 7. Sauvegarde des résultats
    torch.save(model.state_dict(), final_metrics["model_path"])
    
    with open(f"{config['output_dir']}/final_metrics.json", "w") as f:
        json.dump(final_metrics, f, indent=4)

    print("✅ Entraînement de test terminé avec succès !")

if __name__ == "__main__":
    main()