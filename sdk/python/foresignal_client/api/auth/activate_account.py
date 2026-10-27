from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.account_activation import AccountActivation
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: AccountActivation,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/auth/activate",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: AccountActivation,
) -> Response[Any | Problem]:
    """Activate an invited account

     Sets the first password for an invited user from the emailed activation token, moving the account
    from `invited` to `active`. The token is single-use.

    **Scope:** `open`.

    Args:
        body (AccountActivation): Activate an invited account: set the first password with the
            emailed activation token (165/170)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: AccountActivation,
) -> Any | Problem | None:
    """Activate an invited account

     Sets the first password for an invited user from the emailed activation token, moving the account
    from `invited` to `active`. The token is single-use.

    **Scope:** `open`.

    Args:
        body (AccountActivation): Activate an invited account: set the first password with the
            emailed activation token (165/170)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: AccountActivation,
) -> Response[Any | Problem]:
    """Activate an invited account

     Sets the first password for an invited user from the emailed activation token, moving the account
    from `invited` to `active`. The token is single-use.

    **Scope:** `open`.

    Args:
        body (AccountActivation): Activate an invited account: set the first password with the
            emailed activation token (165/170)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: AccountActivation,
) -> Any | Problem | None:
    """Activate an invited account

     Sets the first password for an invited user from the emailed activation token, moving the account
    from `invited` to `active`. The token is single-use.

    **Scope:** `open`.

    Args:
        body (AccountActivation): Activate an invited account: set the first password with the
            emailed activation token (165/170)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
