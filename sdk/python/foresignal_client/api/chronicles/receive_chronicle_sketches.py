from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_sketch_batch import ChronicleSketchBatch
from ...models.chronicle_sketch_batch_result import ChronicleSketchBatchResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ChronicleSketchBatch,
    foresignal_signature: str,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    headers["Foresignal-Signature"] = foresignal_signature

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/federation/chronicle-sketches",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleSketchBatchResult | Problem | None:
    if response.status_code == 202:
        response_202 = ChronicleSketchBatchResult.from_dict(response.json())

        return response_202

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ChronicleSketchBatchResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ChronicleSketchBatch,
    foresignal_signature: str,
) -> Response[ChronicleSketchBatchResult | Problem]:
    """Receive chronicle sketches from a peer

     Peers exchange signed sketch packages for `network`-discoverable chronicles. The body is signed with
    the sender's instance key (109) in `Foresignal-Signature` (detached JWS) and verified against the
    peer's pinned key, else its JWKS. Sketches whose TLP does not allow network discoverability (RED,
    AMBER+STRICT) are rejected one by one. Received sketches expire after 30 days unless refreshed and
    only ever yield opaque links. The same channel carries `accessRequests` and `accessDecisions`
    between instances.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        foresignal_signature (str):
        body (ChronicleSketchBatch): Signed batch of sketch packages sent over the 113 peer
            channel

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSketchBatchResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        foresignal_signature=foresignal_signature,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ChronicleSketchBatch,
    foresignal_signature: str,
) -> ChronicleSketchBatchResult | Problem | None:
    """Receive chronicle sketches from a peer

     Peers exchange signed sketch packages for `network`-discoverable chronicles. The body is signed with
    the sender's instance key (109) in `Foresignal-Signature` (detached JWS) and verified against the
    peer's pinned key, else its JWKS. Sketches whose TLP does not allow network discoverability (RED,
    AMBER+STRICT) are rejected one by one. Received sketches expire after 30 days unless refreshed and
    only ever yield opaque links. The same channel carries `accessRequests` and `accessDecisions`
    between instances.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        foresignal_signature (str):
        body (ChronicleSketchBatch): Signed batch of sketch packages sent over the 113 peer
            channel

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSketchBatchResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
        foresignal_signature=foresignal_signature,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ChronicleSketchBatch,
    foresignal_signature: str,
) -> Response[ChronicleSketchBatchResult | Problem]:
    """Receive chronicle sketches from a peer

     Peers exchange signed sketch packages for `network`-discoverable chronicles. The body is signed with
    the sender's instance key (109) in `Foresignal-Signature` (detached JWS) and verified against the
    peer's pinned key, else its JWKS. Sketches whose TLP does not allow network discoverability (RED,
    AMBER+STRICT) are rejected one by one. Received sketches expire after 30 days unless refreshed and
    only ever yield opaque links. The same channel carries `accessRequests` and `accessDecisions`
    between instances.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        foresignal_signature (str):
        body (ChronicleSketchBatch): Signed batch of sketch packages sent over the 113 peer
            channel

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSketchBatchResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        foresignal_signature=foresignal_signature,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ChronicleSketchBatch,
    foresignal_signature: str,
) -> ChronicleSketchBatchResult | Problem | None:
    """Receive chronicle sketches from a peer

     Peers exchange signed sketch packages for `network`-discoverable chronicles. The body is signed with
    the sender's instance key (109) in `Foresignal-Signature` (detached JWS) and verified against the
    peer's pinned key, else its JWKS. Sketches whose TLP does not allow network discoverability (RED,
    AMBER+STRICT) are rejected one by one. Received sketches expire after 30 days unless refreshed and
    only ever yield opaque links. The same channel carries `accessRequests` and `accessDecisions`
    between instances.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        foresignal_signature (str):
        body (ChronicleSketchBatch): Signed batch of sketch packages sent over the 113 peer
            channel

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSketchBatchResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
            foresignal_signature=foresignal_signature,
        )
    ).parsed
