from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.source_detail import SourceDetail
from ...models.source_settings_update import SourceSettingsUpdate
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: SourceSettingsUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/sources/{source_id}".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SourceDetail | None:
    if response.status_code == 200:
        response_200 = SourceDetail.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[Problem | SourceDetail]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: SourceSettingsUpdate,
) -> Response[Problem | SourceDetail]:
    """Update source settings

     Enable/disable (subscribe/unsubscribe), set interval, params, budget, retention and the ingest-
    policy override. Changes apply immediately; omitted fields are unchanged, an explicit `ingestPolicy:
    null` clears the override. Policy violations return 400 INGEST_POLICY_INVALID with every violation
    in `errors[]` (e.g. `ingestPolicy.keyframe`); retention on a TRACK source returns 400
    RETENTION_TIER_FIXED (use `retention.trackWindow`). A parameter value allowed only on a private
    source (086: `local-remoteid`'s `keepOperatorPosition=true`) returns 400 PRIVATE_REQUIRED unless the
    source is effectively private (descriptor or `private: true`, in the same request or already set),
    and so does making such a source public.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (SourceSettingsUpdate): Partial update; omitted fields unchanged. retention=null
            reverts to the default (400 RETENTION_TIER_FIXED on TRACK sources); ingestPolicy=null
            clears the policy override.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SourceDetail]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: SourceSettingsUpdate,
) -> Problem | SourceDetail | None:
    """Update source settings

     Enable/disable (subscribe/unsubscribe), set interval, params, budget, retention and the ingest-
    policy override. Changes apply immediately; omitted fields are unchanged, an explicit `ingestPolicy:
    null` clears the override. Policy violations return 400 INGEST_POLICY_INVALID with every violation
    in `errors[]` (e.g. `ingestPolicy.keyframe`); retention on a TRACK source returns 400
    RETENTION_TIER_FIXED (use `retention.trackWindow`). A parameter value allowed only on a private
    source (086: `local-remoteid`'s `keepOperatorPosition=true`) returns 400 PRIVATE_REQUIRED unless the
    source is effectively private (descriptor or `private: true`, in the same request or already set),
    and so does making such a source public.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (SourceSettingsUpdate): Partial update; omitted fields unchanged. retention=null
            reverts to the default (400 RETENTION_TIER_FIXED on TRACK sources); ingestPolicy=null
            clears the policy override.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SourceDetail
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: SourceSettingsUpdate,
) -> Response[Problem | SourceDetail]:
    """Update source settings

     Enable/disable (subscribe/unsubscribe), set interval, params, budget, retention and the ingest-
    policy override. Changes apply immediately; omitted fields are unchanged, an explicit `ingestPolicy:
    null` clears the override. Policy violations return 400 INGEST_POLICY_INVALID with every violation
    in `errors[]` (e.g. `ingestPolicy.keyframe`); retention on a TRACK source returns 400
    RETENTION_TIER_FIXED (use `retention.trackWindow`). A parameter value allowed only on a private
    source (086: `local-remoteid`'s `keepOperatorPosition=true`) returns 400 PRIVATE_REQUIRED unless the
    source is effectively private (descriptor or `private: true`, in the same request or already set),
    and so does making such a source public.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (SourceSettingsUpdate): Partial update; omitted fields unchanged. retention=null
            reverts to the default (400 RETENTION_TIER_FIXED on TRACK sources); ingestPolicy=null
            clears the policy override.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SourceDetail]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: SourceSettingsUpdate,
) -> Problem | SourceDetail | None:
    """Update source settings

     Enable/disable (subscribe/unsubscribe), set interval, params, budget, retention and the ingest-
    policy override. Changes apply immediately; omitted fields are unchanged, an explicit `ingestPolicy:
    null` clears the override. Policy violations return 400 INGEST_POLICY_INVALID with every violation
    in `errors[]` (e.g. `ingestPolicy.keyframe`); retention on a TRACK source returns 400
    RETENTION_TIER_FIXED (use `retention.trackWindow`). A parameter value allowed only on a private
    source (086: `local-remoteid`'s `keepOperatorPosition=true`) returns 400 PRIVATE_REQUIRED unless the
    source is effectively private (descriptor or `private: true`, in the same request or already set),
    and so does making such a source public.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (SourceSettingsUpdate): Partial update; omitted fields unchanged. retention=null
            reverts to the default (400 RETENTION_TIER_FIXED on TRACK sources); ingestPolicy=null
            clears the policy override.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SourceDetail
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
