import { useEffect, useState } from 'react';
import { getUsers, createUser, updateUser, deleteUser } from '../../api/adminApi';
import Modal from '../../components/Modal';
import { useToast } from '../../components/Toast';
import Spinner from '../../components/Spinner';

export default function AnnotatorManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [username, setUsername] = useState('');
  const [generatedPassword, setGeneratedPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { addToast } = useToast();

  const fetchUsers = () => {
    setLoading(true);
    getUsers()
      .then((res) => setUsers(res.data.content ?? res.data ?? []))
      .catch(() => addToast('Erreur lors du chargement des annotateurs', 'error'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchUsers(); }, []);

  const openAddModal = () => {
    setEditingUser(null);
    setFirstName('');
    setLastName('');
    setUsername('');
    setGeneratedPassword('');
    setModalOpen(true);
  };

  const openEditModal = (user) => {
    setEditingUser(user);
    setFirstName(user.firstName);
    setLastName(user.lastName);
    setUsername(user.username);
    setGeneratedPassword('');
    setModalOpen(true);
  };

  const handleSubmit = () => {
    if (!firstName || !lastName || !username) {
      addToast('Veuillez remplir tous les champs', 'error');
      return;
    }
    setSubmitting(true);
    const data = { firstName, lastName, username };

    if (editingUser) {
      updateUser(editingUser.id, data)
        .then(() => {
          addToast('Annotateur modifié avec succès', 'success');
          setModalOpen(false);
          fetchUsers();
        })
        .catch(() => addToast('Erreur lors de la modification', 'error'))
        .finally(() => setSubmitting(false));
    } else {
      createUser(data)
        .then((res) => {
          const password = res.data?.password;
          if (password) setGeneratedPassword(password);
          addToast('Annotateur créé avec succès', 'success');
          fetchUsers();
        })
        .catch(() => addToast("Erreur lors de la création de l'annotateur", 'error'))
        .finally(() => setSubmitting(false));
    }
  };

  const handleDelete = (user) => {
    if (!window.confirm(`Supprimer l'annotateur "${user.firstName} ${user.lastName}" ?`)) return;
    deleteUser(user.id)
      .then(() => {
        addToast('Annotateur supprimé avec succès', 'success');
        fetchUsers();
      })
      .catch(() => addToast('Erreur lors de la suppression', 'error'));
  };

  const resetForm = () => {
    setModalOpen(false);
    setEditingUser(null);
    setFirstName('');
    setLastName('');
    setUsername('');
    setGeneratedPassword('');
  };

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Gestion des Annotateurs</h1>
        <button
          onClick={openAddModal}
          className="rounded bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 dark:bg-green-500 dark:hover:bg-green-600"
        >
          + Ajouter
        </button>
      </div>

      {loading ? (
        <Spinner size="lg" className="mt-20" />
      ) : users.length === 0 ? (
        <p className="mt-20 text-center text-gray-500 dark:text-gray-400">Aucun annotateur trouvé.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-700 dark:text-gray-300">
            <thead>
              <tr className="border-b border-gray-200 dark:border-gray-700">
                <th className="pb-3 pr-4 font-semibold">Prénom</th>
                <th className="pb-3 pr-4 font-semibold">Nom</th>
                <th className="pb-3 font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-b border-gray-100 dark:border-gray-800">
                  <td className="py-3 pr-4">{user.firstName}</td>
                  <td className="py-3 pr-4">{user.lastName}</td>
                  <td className="py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() => openEditModal(user)}
                        className="rounded bg-green-600 px-3 py-1 text-xs font-medium text-white hover:bg-green-700 dark:bg-green-500 dark:hover:bg-green-600"
                      >
                        Modifier
                      </button>
                      <button
                        onClick={() => handleDelete(user)}
                        className="rounded bg-red-600 px-3 py-1 text-xs font-medium text-white hover:bg-red-700 dark:bg-red-500 dark:hover:bg-red-600"
                      >
                        Supprimer
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={resetForm} title={editingUser ? 'Modifier un annotateur' : 'Ajouter un annotateur'}>
        <form
          onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}
          className="flex flex-col gap-4"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Nom</label>
            <input
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              className="w-full rounded border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Prénom</label>
            <input
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              className="w-full rounded border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Login</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full rounded border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>
          {generatedPassword && (
            <div className="rounded bg-green-100 dark:bg-green-900/30 border border-green-300 dark:border-green-700 p-3 text-sm text-green-800 dark:text-green-300">
              Annotateur créé. Mot de passe généré : <strong>{generatedPassword}</strong>
            </div>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={resetForm}
              className="rounded border border-gray-300 dark:border-gray-600 px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
            >
              Annuler
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="rounded bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 dark:bg-green-500 dark:hover:bg-green-600 disabled:opacity-50"
            >
              {submitting ? 'Enregistrement...' : 'Valider'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
