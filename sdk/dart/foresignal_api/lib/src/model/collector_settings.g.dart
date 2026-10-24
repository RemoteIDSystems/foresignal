// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collector_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectorSettingsCWProxy {
  CollectorSettings maxConcurrentPulls(int maxConcurrentPulls);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectorSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectorSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectorSettings call({int maxConcurrentPulls});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectorSettings.copyWith(...)` or call `instanceOfCollectorSettings.copyWith.fieldName(value)` for a single field.
class _$CollectorSettingsCWProxyImpl implements _$CollectorSettingsCWProxy {
  const _$CollectorSettingsCWProxyImpl(this._value);

  final CollectorSettings _value;

  @override
  CollectorSettings maxConcurrentPulls(int maxConcurrentPulls) =>
      call(maxConcurrentPulls: maxConcurrentPulls);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectorSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectorSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectorSettings call({
    Object? maxConcurrentPulls = const $CopyWithPlaceholder(),
  }) {
    return CollectorSettings(
      maxConcurrentPulls:
          maxConcurrentPulls == const $CopyWithPlaceholder() ||
              maxConcurrentPulls == null
          ? _value.maxConcurrentPulls
          // ignore: cast_nullable_to_non_nullable
          : maxConcurrentPulls as int,
    );
  }
}

extension $CollectorSettingsCopyWith on CollectorSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectorSettings.copyWith(...)` or `instanceOfCollectorSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectorSettingsCWProxy get copyWith =>
      _$CollectorSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectorSettings _$CollectorSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollectorSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['maxConcurrentPulls']);
      final val = CollectorSettings(
        maxConcurrentPulls: $checkedConvert(
          'maxConcurrentPulls',
          (v) => (v as num?)?.toInt() ?? 2,
        ),
      );
      return val;
    });

Map<String, dynamic> _$CollectorSettingsToJson(CollectorSettings instance) =>
    <String, dynamic>{'maxConcurrentPulls': instance.maxConcurrentPulls};
