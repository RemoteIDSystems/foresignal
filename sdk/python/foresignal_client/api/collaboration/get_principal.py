from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.principal import Principal
from ...models.principal_summary import PrincipalSummary
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    principal_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/principals/{principal_id}".format(
            principal_id=quote(str(principal_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Principal | PrincipalSummary | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(data: object) -> Principal | PrincipalSummary:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = Principal.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_1 = PrincipalSummary.from_dict(data)

            return response_200_type_1

        response_200 = _parse_response_200(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Principal | PrincipalSummary | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    principal_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Principal | PrincipalSummary | Problem]:
    """Get a principal

     Returns the summary of a principal by id or handle; admins and the principal itself get the full
    `Principal`.

    **Scope:** `read`.

    Args:
        principal_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Principal | PrincipalSummary | Problem]
    """

    kwargs = _get_kwargs(
        principal_id=principal_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    principal_id: str,
    *,
    client: AuthenticatedClient,
) -> Principal | PrincipalSummary | Problem | None:
    """Get a principal

     Returns the summary of a principal by id or handle; admins and the principal itself get the full
    `Principal`.

    **Scope:** `read`.

    Args:
        principal_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Principal | PrincipalSummary | Problem
    """

    return sync_detailed(
        principal_id=principal_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    principal_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Principal | PrincipalSummary | Problem]:
    """Get a principal

     Returns the summary of a principal by id or handle; admins and the principal itself get the full
    `Principal`.

    **Scope:** `read`.

    Args:
        principal_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Principal | PrincipalSummary | Problem]
    """

    kwargs = _get_kwargs(
        principal_id=principal_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    principal_id: str,
    *,
    client: AuthenticatedClient,
) -> Principal | PrincipalSummary | Problem | None:
    """Get a principal

     Returns the summary of a principal by id or handle; admins and the principal itself get the full
    `Principal`.

    **Scope:** `read`.

    Args:
        principal_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Principal | PrincipalSummary | Problem
    """

    return (
        await asyncio_detailed(
            principal_id=principal_id,
            client=client,
        )
    ).parsed
