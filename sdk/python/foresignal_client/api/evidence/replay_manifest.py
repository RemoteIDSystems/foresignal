from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.replay_result import ReplayResult
from ...types import Response


def _get_kwargs(
    manifest_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/manifests/{manifest_id}/replay".format(
            manifest_id=quote(str(manifest_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReplayResult | None:
    if response.status_code == 202:
        response_202 = ReplayResult.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ReplayResult]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    manifest_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ReplayResult]:
    """Replay an analysis manifest

     Queues a job (103 framework) that re-runs a deterministic component at the manifest's asOfSeq with
    the recorded configuration and inputs and reports identical, differs (with a diff summary) or not-
    replayable. LLM components report 'inputs recorded; model output not reproducible'. Errors: `404
    UNKNOWN_MANIFEST`, `409 MANIFEST_NOT_REPLAYABLE` when known up front.

    **Scope:** `agent`.

    Args:
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReplayResult]
    """

    kwargs = _get_kwargs(
        manifest_id=manifest_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    manifest_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ReplayResult | None:
    """Replay an analysis manifest

     Queues a job (103 framework) that re-runs a deterministic component at the manifest's asOfSeq with
    the recorded configuration and inputs and reports identical, differs (with a diff summary) or not-
    replayable. LLM components report 'inputs recorded; model output not reproducible'. Errors: `404
    UNKNOWN_MANIFEST`, `409 MANIFEST_NOT_REPLAYABLE` when known up front.

    **Scope:** `agent`.

    Args:
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReplayResult
    """

    return sync_detailed(
        manifest_id=manifest_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    manifest_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ReplayResult]:
    """Replay an analysis manifest

     Queues a job (103 framework) that re-runs a deterministic component at the manifest's asOfSeq with
    the recorded configuration and inputs and reports identical, differs (with a diff summary) or not-
    replayable. LLM components report 'inputs recorded; model output not reproducible'. Errors: `404
    UNKNOWN_MANIFEST`, `409 MANIFEST_NOT_REPLAYABLE` when known up front.

    **Scope:** `agent`.

    Args:
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReplayResult]
    """

    kwargs = _get_kwargs(
        manifest_id=manifest_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    manifest_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ReplayResult | None:
    """Replay an analysis manifest

     Queues a job (103 framework) that re-runs a deterministic component at the manifest's asOfSeq with
    the recorded configuration and inputs and reports identical, differs (with a diff summary) or not-
    replayable. LLM components report 'inputs recorded; model output not reproducible'. Errors: `404
    UNKNOWN_MANIFEST`, `409 MANIFEST_NOT_REPLAYABLE` when known up front.

    **Scope:** `agent`.

    Args:
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReplayResult
    """

    return (
        await asyncio_detailed(
            manifest_id=manifest_id,
            client=client,
        )
    ).parsed
