var React = require('react');
var ReactDOM = require('react-dom');
var expect = require('expect');
var $ = require('jQuery');
var TestUtils = require('react-addons-test-utils');

import { RequestResetPassword } from '../../../main/access/authentication/RequestResetPassword';

describe('RequestResetPassword', () => {

  it('should exist', () => {
    expect(RequestResetPassword).toExist();
  });

});
