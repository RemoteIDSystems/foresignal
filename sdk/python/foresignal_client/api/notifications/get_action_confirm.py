from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    token: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/actions/{token}".format(
            token=quote(str(token), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

    if response.status_code == 410:
        response_410 = response.text
        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    token: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | str]:
    """Show an action confirm page

     Never acts. Returns a minimal server-rendered HTML page (alert title, severity, fired time, action
    and duration, one POST button; no JavaScript or external assets) or a 410 page. The MAC is verified
    before any lookup. Limited to 30 requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        token=token,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    token: str,
    *,
    client: AuthenticatedClient,
) -> Problem | str | None:
    """Show an action confirm page

     Never acts. Returns a minimal server-rendered HTML page (alert title, severity, fired time, action
    and duration, one POST button; no JavaScript or external assets) or a 410 page. The MAC is verified
    before any lookup. Limited to 30 requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return sync_detailed(
        token=token,
        client=client,
    ).parsed


async def asyncio_detailed(
    token: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | str]:
    """Show an action confirm page

     Never acts. Returns a minimal server-rendered HTML page (alert title, severity, fired time, action
    and duration, one POST button; no JavaScript or external assets) or a 410 page. The MAC is verified
    before any lookup. Limited to 30 requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        token=token,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    token: str,
    *,
    client: AuthenticatedClient,
) -> Problem | str | None:
    """Show an action confirm page

     Never acts. Returns a minimal server-rendered HTML page (alert title, severity, fired time, action
    and duration, one POST button; no JavaScript or external assets) or a 410 page. The MAC is verified
    before any lookup. Limited to 30 requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return (
        await asyncio_detailed(
            token=token,
            client=client,
        )
    ).parsed
