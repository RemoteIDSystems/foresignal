// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'access_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AccessRequestCWProxy {
  AccessRequest id(String id);

  AccessRequest linkId(String linkId);

  AccessRequest direction(AccessRequestDirectionEnum direction);

  AccessRequest chronicleId(String? chronicleId);

  AccessRequest requesterHandle(String requesterHandle);

  AccessRequest requesterInstanceId(String? requesterInstanceId);

  AccessRequest message(String? message);

  AccessRequest offerShare(AccessRequestOfferShare? offerShare);

  AccessRequest status(AccessRequestStatusEnum status);

  AccessRequest createdAt(DateTime createdAt);

  AccessRequest decidedAt(DateTime? decidedAt);

  AccessRequest grant(AccessRequestGrant? grant);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  AccessRequest call({
    String id,
    String linkId,
    AccessRequestDirectionEnum direction,
    String? chronicleId,
    String requesterHandle,
    String? requesterInstanceId,
    String? message,
    AccessRequestOfferShare? offerShare,
    AccessRequestStatusEnum status,
    DateTime createdAt,
    DateTime? decidedAt,
    AccessRequestGrant? grant,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAccessRequest.copyWith(...)` or call `instanceOfAccessRequest.copyWith.fieldName(value)` for a single field.
class _$AccessRequestCWProxyImpl implements _$AccessRequestCWProxy {
  const _$AccessRequestCWProxyImpl(this._value);

  final AccessRequest _value;

  @override
  AccessRequest id(String id) => call(id: id);

  @override
  AccessRequest linkId(String linkId) => call(linkId: linkId);

  @override
  AccessRequest direction(AccessRequestDirectionEnum direction) =>
      call(direction: direction);

  @override
  AccessRequest chronicleId(String? chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  AccessRequest requesterHandle(String requesterHandle) =>
      call(requesterHandle: requesterHandle);

  @override
  AccessRequest requesterInstanceId(String? requesterInstanceId) =>
      call(requesterInstanceId: requesterInstanceId);

  @override
  AccessRequest message(String? message) => call(message: message);

  @override
  AccessRequest offerShare(AccessRequestOfferShare? offerShare) =>
      call(offerShare: offerShare);

  @override
  AccessRequest status(AccessRequestStatusEnum status) => call(status: status);

  @override
  AccessRequest createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  AccessRequest decidedAt(DateTime? decidedAt) => call(decidedAt: decidedAt);

  @override
  AccessRequest grant(AccessRequestGrant? grant) => call(grant: grant);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AccessRequest call({
    Object? id = const $CopyWithPlaceholder(),
    Object? linkId = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? requesterHandle = const $CopyWithPlaceholder(),
    Object? requesterInstanceId = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? offerShare = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? decidedAt = const $CopyWithPlaceholder(),
    Object? grant = const $CopyWithPlaceholder(),
  }) {
    return AccessRequest(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      linkId: linkId == const $CopyWithPlaceholder() || linkId == null
          ? _value.linkId
          // ignore: cast_nullable_to_non_nullable
          : linkId as String,
      direction: direction == const $CopyWithPlaceholder() || direction == null
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as AccessRequestDirectionEnum,
      chronicleId: chronicleId == const $CopyWithPlaceholder()
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String?,
      requesterHandle:
          requesterHandle == const $CopyWithPlaceholder() ||
              requesterHandle == null
          ? _value.requesterHandle
          // ignore: cast_nullable_to_non_nullable
          : requesterHandle as String,
      requesterInstanceId: requesterInstanceId == const $CopyWithPlaceholder()
          ? _value.requesterInstanceId
          // ignore: cast_nullable_to_non_nullable
          : requesterInstanceId as String?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      offerShare: offerShare == const $CopyWithPlaceholder()
          ? _value.offerShare
          // ignore: cast_nullable_to_non_nullable
          : offerShare as AccessRequestOfferShare?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AccessRequestStatusEnum,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      decidedAt: decidedAt == const $CopyWithPlaceholder()
          ? _value.decidedAt
          // ignore: cast_nullable_to_non_nullable
          : decidedAt as DateTime?,
      grant: grant == const $CopyWithPlaceholder()
          ? _value.grant
          // ignore: cast_nullable_to_non_nullable
          : grant as AccessRequestGrant?,
    );
  }
}

extension $AccessRequestCopyWith on AccessRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAccessRequest.copyWith(...)` or `instanceOfAccessRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AccessRequestCWProxy get copyWith => _$AccessRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccessRequest _$AccessRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AccessRequest', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'linkId',
          'direction',
          'requesterHandle',
          'status',
          'createdAt',
        ],
      );
      final val = AccessRequest(
        id: $checkedConvert('id', (v) => v as String),
        linkId: $checkedConvert('linkId', (v) => v as String),
        direction: $checkedConvert(
          'direction',
          (v) => $enumDecode(_$AccessRequestDirectionEnumEnumMap, v),
        ),
        chronicleId: $checkedConvert('chronicleId', (v) => v as String?),
        requesterHandle: $checkedConvert('requesterHandle', (v) => v as String),
        requesterInstanceId: $checkedConvert(
          'requesterInstanceId',
          (v) => v as String?,
        ),
        message: $checkedConvert('message', (v) => v as String?),
        offerShare: $checkedConvert(
          'offerShare',
          (v) => v == null
              ? null
              : AccessRequestOfferShare.fromJson(v as Map<String, dynamic>),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$AccessRequestStatusEnumEnumMap, v),
        ),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        decidedAt: $checkedConvert(
          'decidedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        grant: $checkedConvert(
          'grant',
          (v) => v == null
              ? null
              : AccessRequestGrant.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AccessRequestToJson(AccessRequest instance) =>
    <String, dynamic>{
      'id': instance.id,
      'linkId': instance.linkId,
      'direction': _$AccessRequestDirectionEnumEnumMap[instance.direction]!,
      'chronicleId': ?instance.chronicleId,
      'requesterHandle': instance.requesterHandle,
      'requesterInstanceId': ?instance.requesterInstanceId,
      'message': ?instance.message,
      'offerShare': ?instance.offerShare?.toJson(),
      'status': _$AccessRequestStatusEnumEnumMap[instance.status]!,
      'createdAt': instance.createdAt.toIso8601String(),
      'decidedAt': ?instance.decidedAt?.toIso8601String(),
      'grant': ?instance.grant?.toJson(),
    };

const _$AccessRequestDirectionEnumEnumMap = {
  AccessRequestDirectionEnum.incoming: 'incoming',
  AccessRequestDirectionEnum.outgoing: 'outgoing',
};

const _$AccessRequestStatusEnumEnumMap = {
  AccessRequestStatusEnum.pending: 'pending',
  AccessRequestStatusEnum.granted: 'granted',
  AccessRequestStatusEnum.declined: 'declined',
};
