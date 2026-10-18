// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_access_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleAccessEventCWProxy {
  ChronicleAccessEvent requestId(String requestId);

  ChronicleAccessEvent linkId(String linkId);

  ChronicleAccessEvent status(ChronicleAccessEventStatusEnum status);

  ChronicleAccessEvent requesterHandle(String? requesterHandle);

  ChronicleAccessEvent message(String? message);

  ChronicleAccessEvent chronicleId(String? chronicleId);

  ChronicleAccessEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleAccessEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleAccessEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleAccessEvent call({
    String requestId,
    String linkId,
    ChronicleAccessEventStatusEnum status,
    String? requesterHandle,
    String? message,
    String? chronicleId,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleAccessEvent.copyWith(...)` or call `instanceOfChronicleAccessEvent.copyWith.fieldName(value)` for a single field.
class _$ChronicleAccessEventCWProxyImpl
    implements _$ChronicleAccessEventCWProxy {
  const _$ChronicleAccessEventCWProxyImpl(this._value);

  final ChronicleAccessEvent _value;

  @override
  ChronicleAccessEvent requestId(String requestId) =>
      call(requestId: requestId);

  @override
  ChronicleAccessEvent linkId(String linkId) => call(linkId: linkId);

  @override
  ChronicleAccessEvent status(ChronicleAccessEventStatusEnum status) =>
      call(status: status);

  @override
  ChronicleAccessEvent requesterHandle(String? requesterHandle) =>
      call(requesterHandle: requesterHandle);

  @override
  ChronicleAccessEvent message(String? message) => call(message: message);

  @override
  ChronicleAccessEvent chronicleId(String? chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleAccessEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleAccessEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleAccessEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleAccessEvent call({
    Object? requestId = const $CopyWithPlaceholder(),
    Object? linkId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? requesterHandle = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ChronicleAccessEvent(
      requestId: requestId == const $CopyWithPlaceholder() || requestId == null
          ? _value.requestId
          // ignore: cast_nullable_to_non_nullable
          : requestId as String,
      linkId: linkId == const $CopyWithPlaceholder() || linkId == null
          ? _value.linkId
          // ignore: cast_nullable_to_non_nullable
          : linkId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ChronicleAccessEventStatusEnum,
      requesterHandle: requesterHandle == const $CopyWithPlaceholder()
          ? _value.requesterHandle
          // ignore: cast_nullable_to_non_nullable
          : requesterHandle as String?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      chronicleId: chronicleId == const $CopyWithPlaceholder()
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ChronicleAccessEventCopyWith on ChronicleAccessEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleAccessEvent.copyWith(...)` or `instanceOfChronicleAccessEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleAccessEventCWProxy get copyWith =>
      _$ChronicleAccessEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleAccessEvent _$ChronicleAccessEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleAccessEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['requestId', 'linkId', 'status', 'at']);
  final val = ChronicleAccessEvent(
    requestId: $checkedConvert('requestId', (v) => v as String),
    linkId: $checkedConvert('linkId', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ChronicleAccessEventStatusEnumEnumMap, v),
    ),
    requesterHandle: $checkedConvert('requesterHandle', (v) => v as String?),
    message: $checkedConvert('message', (v) => v as String?),
    chronicleId: $checkedConvert('chronicleId', (v) => v as String?),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ChronicleAccessEventToJson(
  ChronicleAccessEvent instance,
) => <String, dynamic>{
  'requestId': instance.requestId,
  'linkId': instance.linkId,
  'status': _$ChronicleAccessEventStatusEnumEnumMap[instance.status]!,
  'requesterHandle': ?instance.requesterHandle,
  'message': ?instance.message,
  'chronicleId': ?instance.chronicleId,
  'at': instance.at.toIso8601String(),
};

const _$ChronicleAccessEventStatusEnumEnumMap = {
  ChronicleAccessEventStatusEnum.pending: 'pending',
  ChronicleAccessEventStatusEnum.granted: 'granted',
  ChronicleAccessEventStatusEnum.declined: 'declined',
};
