// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_forecaster_registration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalForecasterRegistrationCWProxy {
  ExternalForecasterRegistration id(String id);

  ExternalForecasterRegistration name(String? name);

  ExternalForecasterRegistration config(Map<String, Object>? config);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalForecasterRegistration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalForecasterRegistration(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalForecasterRegistration call({
    String id,
    String? name,
    Map<String, Object>? config,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalForecasterRegistration.copyWith(...)` or call `instanceOfExternalForecasterRegistration.copyWith.fieldName(value)` for a single field.
class _$ExternalForecasterRegistrationCWProxyImpl
    implements _$ExternalForecasterRegistrationCWProxy {
  const _$ExternalForecasterRegistrationCWProxyImpl(this._value);

  final ExternalForecasterRegistration _value;

  @override
  ExternalForecasterRegistration id(String id) => call(id: id);

  @override
  ExternalForecasterRegistration name(String? name) => call(name: name);

  @override
  ExternalForecasterRegistration config(Map<String, Object>? config) =>
      call(config: config);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalForecasterRegistration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalForecasterRegistration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalForecasterRegistration call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
  }) {
    return ExternalForecasterRegistration(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
    );
  }
}

extension $ExternalForecasterRegistrationCopyWith
    on ExternalForecasterRegistration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalForecasterRegistration.copyWith(...)` or `instanceOfExternalForecasterRegistration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalForecasterRegistrationCWProxy get copyWith =>
      _$ExternalForecasterRegistrationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalForecasterRegistration _$ExternalForecasterRegistrationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalForecasterRegistration', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id']);
  final val = ExternalForecasterRegistration(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalForecasterRegistrationToJson(
  ExternalForecasterRegistration instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': ?instance.name,
  'config': ?instance.config,
};
