// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_route_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressRouteInputCWProxy {
  EgressRouteInput name(String name);

  EgressRouteInput type(String type);

  EgressRouteInput enabled(bool? enabled);

  EgressRouteInput url(String? url);

  EgressRouteInput boundInterface(String? boundInterface);

  EgressRouteInput secrets(EgressRouteInputSecrets? secrets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRouteInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRouteInput(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressRouteInput call({
    String name,
    String type,
    bool? enabled,
    String? url,
    String? boundInterface,
    EgressRouteInputSecrets? secrets,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressRouteInput.copyWith(...)` or call `instanceOfEgressRouteInput.copyWith.fieldName(value)` for a single field.
class _$EgressRouteInputCWProxyImpl implements _$EgressRouteInputCWProxy {
  const _$EgressRouteInputCWProxyImpl(this._value);

  final EgressRouteInput _value;

  @override
  EgressRouteInput name(String name) => call(name: name);

  @override
  EgressRouteInput type(String type) => call(type: type);

  @override
  EgressRouteInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  EgressRouteInput url(String? url) => call(url: url);

  @override
  EgressRouteInput boundInterface(String? boundInterface) =>
      call(boundInterface: boundInterface);

  @override
  EgressRouteInput secrets(EgressRouteInputSecrets? secrets) =>
      call(secrets: secrets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRouteInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRouteInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressRouteInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? boundInterface = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
  }) {
    return EgressRouteInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      boundInterface: boundInterface == const $CopyWithPlaceholder()
          ? _value.boundInterface
          // ignore: cast_nullable_to_non_nullable
          : boundInterface as String?,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as EgressRouteInputSecrets?,
    );
  }
}

extension $EgressRouteInputCopyWith on EgressRouteInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressRouteInput.copyWith(...)` or `instanceOfEgressRouteInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressRouteInputCWProxy get copyWith => _$EgressRouteInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressRouteInput _$EgressRouteInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EgressRouteInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'type']);
      final val = EgressRouteInput(
        name: $checkedConvert('name', (v) => v as String),
        type: $checkedConvert('type', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
        url: $checkedConvert('url', (v) => v as String?),
        boundInterface: $checkedConvert('boundInterface', (v) => v as String?),
        secrets: $checkedConvert(
          'secrets',
          (v) => v == null
              ? null
              : EgressRouteInputSecrets.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EgressRouteInputToJson(EgressRouteInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'type': instance.type,
      'enabled': ?instance.enabled,
      'url': ?instance.url,
      'boundInterface': ?instance.boundInterface,
      'secrets': ?instance.secrets?.toJson(),
    };
