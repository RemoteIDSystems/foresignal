// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'plugin_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PluginUpdateCWProxy {
  PluginUpdate enabled(bool enabled);

  PluginUpdate sha256(String? sha256);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PluginUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PluginUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  PluginUpdate call({bool enabled, String? sha256});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPluginUpdate.copyWith(...)` or call `instanceOfPluginUpdate.copyWith.fieldName(value)` for a single field.
class _$PluginUpdateCWProxyImpl implements _$PluginUpdateCWProxy {
  const _$PluginUpdateCWProxyImpl(this._value);

  final PluginUpdate _value;

  @override
  PluginUpdate enabled(bool enabled) => call(enabled: enabled);

  @override
  PluginUpdate sha256(String? sha256) => call(sha256: sha256);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PluginUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PluginUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PluginUpdate call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
  }) {
    return PluginUpdate(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      sha256: sha256 == const $CopyWithPlaceholder()
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String?,
    );
  }
}

extension $PluginUpdateCopyWith on PluginUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPluginUpdate.copyWith(...)` or `instanceOfPluginUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PluginUpdateCWProxy get copyWith => _$PluginUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PluginUpdate _$PluginUpdateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PluginUpdate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled']);
      final val = PluginUpdate(
        enabled: $checkedConvert('enabled', (v) => v as bool),
        sha256: $checkedConvert('sha256', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PluginUpdateToJson(PluginUpdate instance) =>
    <String, dynamic>{'enabled': instance.enabled, 'sha256': ?instance.sha256};
