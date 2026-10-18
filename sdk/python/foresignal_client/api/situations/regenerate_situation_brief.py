from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.situation_brief_status import SituationBriefStatus
from ...types import Response


def _get_kwargs(
    situation_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/situations/{situation_id}/brief".format(
            situation_id=quote(str(situation_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SituationBriefStatus | None:
    if response.status_code == 202:
        response_202 = SituationBriefStatus.from_dict(response.json())

        return response_202

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SituationBriefStatus]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SituationBriefStatus]:
    """Regenerate the situation brief

     Queues an immediate regeneration of the optional LLM brief (028 tool loop, 102 output validator,
    effort `low`). The brief never changes members, status or severity; on failure or budget refusal the
    previous brief stays. Requires a chat provider and `situation.briefs.enabled=true` (else 409
    `LLM_NOT_CONFIGURED`).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationBriefStatus]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SituationBriefStatus | None:
    """Regenerate the situation brief

     Queues an immediate regeneration of the optional LLM brief (028 tool loop, 102 output validator,
    effort `low`). The brief never changes members, status or severity; on failure or budget refusal the
    previous brief stays. Requires a chat provider and `situation.briefs.enabled=true` (else 409
    `LLM_NOT_CONFIGURED`).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationBriefStatus
    """

    return sync_detailed(
        situation_id=situation_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SituationBriefStatus]:
    """Regenerate the situation brief

     Queues an immediate regeneration of the optional LLM brief (028 tool loop, 102 output validator,
    effort `low`). The brief never changes members, status or severity; on failure or budget refusal the
    previous brief stays. Requires a chat provider and `situation.briefs.enabled=true` (else 409
    `LLM_NOT_CONFIGURED`).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationBriefStatus]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SituationBriefStatus | None:
    """Regenerate the situation brief

     Queues an immediate regeneration of the optional LLM brief (028 tool loop, 102 output validator,
    effort `low`). The brief never changes members, status or severity; on failure or budget refusal the
    previous brief stays. Requires a chat provider and `situation.briefs.enabled=true` (else 409
    `LLM_NOT_CONFIGURED`).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationBriefStatus
    """

    return (
        await asyncio_detailed(
            situation_id=situation_id,
            client=client,
        )
    ).parsed
