// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_provider_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaProviderInputCWProxy {
  MediaProviderInput enabled(bool? enabled);

  MediaProviderInput licenceAcknowledged(bool? licenceAcknowledged);

  MediaProviderInput permissionRecorded(bool? permissionRecorded);

  MediaProviderInput apiKey(String? apiKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaProviderInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaProviderInput(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaProviderInput call({
    bool? enabled,
    bool? licenceAcknowledged,
    bool? permissionRecorded,
    String? apiKey,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaProviderInput.copyWith(...)` or call `instanceOfMediaProviderInput.copyWith.fieldName(value)` for a single field.
class _$MediaProviderInputCWProxyImpl implements _$MediaProviderInputCWProxy {
  const _$MediaProviderInputCWProxyImpl(this._value);

  final MediaProviderInput _value;

  @override
  MediaProviderInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  MediaProviderInput licenceAcknowledged(bool? licenceAcknowledged) =>
      call(licenceAcknowledged: licenceAcknowledged);

  @override
  MediaProviderInput permissionRecorded(bool? permissionRecorded) =>
      call(permissionRecorded: permissionRecorded);

  @override
  MediaProviderInput apiKey(String? apiKey) => call(apiKey: apiKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaProviderInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaProviderInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaProviderInput call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? licenceAcknowledged = const $CopyWithPlaceholder(),
    Object? permissionRecorded = const $CopyWithPlaceholder(),
    Object? apiKey = const $CopyWithPlaceholder(),
  }) {
    return MediaProviderInput(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      licenceAcknowledged: licenceAcknowledged == const $CopyWithPlaceholder()
          ? _value.licenceAcknowledged
          // ignore: cast_nullable_to_non_nullable
          : licenceAcknowledged as bool?,
      permissionRecorded: permissionRecorded == const $CopyWithPlaceholder()
          ? _value.permissionRecorded
          // ignore: cast_nullable_to_non_nullable
          : permissionRecorded as bool?,
      apiKey: apiKey == const $CopyWithPlaceholder()
          ? _value.apiKey
          // ignore: cast_nullable_to_non_nullable
          : apiKey as String?,
    );
  }
}

extension $MediaProviderInputCopyWith on MediaProviderInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaProviderInput.copyWith(...)` or `instanceOfMediaProviderInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaProviderInputCWProxy get copyWith =>
      _$MediaProviderInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaProviderInput _$MediaProviderInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaProviderInput', json, ($checkedConvert) {
      final val = MediaProviderInput(
        enabled: $checkedConvert('enabled', (v) => v as bool?),
        licenceAcknowledged: $checkedConvert(
          'licenceAcknowledged',
          (v) => v as bool?,
        ),
        permissionRecorded: $checkedConvert(
          'permissionRecorded',
          (v) => v as bool?,
        ),
        apiKey: $checkedConvert('apiKey', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MediaProviderInputToJson(MediaProviderInput instance) =>
    <String, dynamic>{
      'enabled': ?instance.enabled,
      'licenceAcknowledged': ?instance.licenceAcknowledged,
      'permissionRecorded': ?instance.permissionRecorded,
      'apiKey': ?instance.apiKey,
    };
