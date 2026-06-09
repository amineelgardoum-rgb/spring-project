import json
import sys
import time
import random

def main():
    hyperparameters = {}
    if len(sys.argv) > 1:
        try:
            hyperparameters = json.loads(sys.argv[1])
        except json.JSONDecodeError:
            pass

    learning_rate = hyperparameters.get("learningRate", 0.001)
    epochs = hyperparameters.get("epochs", 10)
    batch_size = hyperparameters.get("batchSize", 32)

    print(f"[INFO] Starting training with lr={learning_rate}, epochs={epochs}, batch_size={batch_size}")
    sys.stdout.flush()

    for epoch in range(1, epochs + 1):
        time.sleep(0.5)
        loss = 1.0 / (epoch + 1) + random.uniform(-0.05, 0.05)
        accuracy = 1.0 - loss
        print(f"[INFO] Epoch {epoch}/{epochs} - loss: {loss:.4f} - accuracy: {accuracy:.4f}")
        sys.stdout.flush()

    final_accuracy = 0.85 + random.uniform(-0.05, 0.05)
    final_f1 = 0.83 + random.uniform(-0.05, 0.05)

    result = {
        "accuracy": round(final_accuracy, 4),
        "f1Score": round(final_f1, 4),
        "epochsCompleted": epochs,
        "hyperparameters": hyperparameters
    }

    print(json.dumps(result))
    sys.stdout.flush()

if __name__ == "__main__":
    main()
