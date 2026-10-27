// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressSettingsCWProxy {
  EgressSettings rotation(String rotation);

  EgressSettings defaultRoute(String? defaultRoute);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressSettings call({String rotation, String? defaultRoute});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressSettings.copyWith(...)` or call `instanceOfEgressSettings.copyWith.fieldName(value)` for a single field.
class _$EgressSettingsCWProxyImpl implements _$EgressSettingsCWProxy {
  const _$EgressSettingsCWProxyImpl(this._value);

  final EgressSettings _value;

  @override
  EgressSettings rotation(String rotation) => call(rotation: rotation);

  @override
  EgressSettings defaultRoute(String? defaultRoute) =>
      call(defaultRoute: defaultRoute);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressSettings call({
    Object? rotation = const $CopyWithPlaceholder(),
    Object? defaultRoute = const $CopyWithPlaceholder(),
  }) {
    return EgressSettings(
      rotation: rotation == const $CopyWithPlaceholder() || rotation == null
          ? _value.rotation
          // ignore: cast_nullable_to_non_nullable
          : rotation as String,
      defaultRoute: defaultRoute == const $CopyWithPlaceholder()
          ? _value.defaultRoute
          // ignore: cast_nullable_to_non_nullable
          : defaultRoute as String?,
    );
  }
}

extension $EgressSettingsCopyWith on EgressSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressSettings.copyWith(...)` or `instanceOfEgressSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressSettingsCWProxy get copyWith => _$EgressSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressSettings _$EgressSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EgressSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['rotation']);
      final val = EgressSettings(
        rotation: $checkedConvert('rotation', (v) => v as String? ?? 'direct'),
        defaultRoute: $checkedConvert('defaultRoute', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EgressSettingsToJson(EgressSettings instance) =>
    <String, dynamic>{
      'rotation': instance.rotation,
      'defaultRoute': ?instance.defaultRoute,
    };
