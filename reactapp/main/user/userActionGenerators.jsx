import axios from 'axios';

import { ROOT_URL } from '../ConfigurationPaths';

const CHANGE_PASSWORD_ENDPOINT = '/change-password';

export const startChangingPassword = function(currentPassword, newPassword) {
  return (dispatch, getState) => {
    return axios.post(`${ROOT_URL}${CHANGE_PASSWORD_ENDPOINT}`,
      {currentPassword, newPassword},
      { headers: { authorization: "Bearer " + localStorage.getItem('token') }});
  };
};
