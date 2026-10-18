// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_provider_test_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushProviderTestRequestCWProxy {
  PushProviderTestRequest deviceId(String? deviceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  PushProviderTestRequest call({String? deviceId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushProviderTestRequest.copyWith(...)` or call `instanceOfPushProviderTestRequest.copyWith.fieldName(value)` for a single field.
class _$PushProviderTestRequestCWProxyImpl
    implements _$PushProviderTestRequestCWProxy {
  const _$PushProviderTestRequestCWProxyImpl(this._value);

  final PushProviderTestRequest _value;

  @override
  PushProviderTestRequest deviceId(String? deviceId) =>
      call(deviceId: deviceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushProviderTestRequest call({
    Object? deviceId = const $CopyWithPlaceholder(),
  }) {
    return PushProviderTestRequest(
      deviceId: deviceId == const $CopyWithPlaceholder()
          ? _value.deviceId
          // ignore: cast_nullable_to_non_nullable
          : deviceId as String?,
    );
  }
}

extension $PushProviderTestRequestCopyWith on PushProviderTestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushProviderTestRequest.copyWith(...)` or `instanceOfPushProviderTestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushProviderTestRequestCWProxy get copyWith =>
      _$PushProviderTestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushProviderTestRequest _$PushProviderTestRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushProviderTestRequest', json, ($checkedConvert) {
  final val = PushProviderTestRequest(
    deviceId: $checkedConvert('deviceId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PushProviderTestRequestToJson(
  PushProviderTestRequest instance,
) => <String, dynamic>{'deviceId': ?instance.deviceId};
