// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecaster_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecasterUpdateCWProxy {
  ForecasterUpdate active(bool active);

  ForecasterUpdate config(Map<String, Object>? config);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecasterUpdate call({bool active, Map<String, Object>? config});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecasterUpdate.copyWith(...)` or call `instanceOfForecasterUpdate.copyWith.fieldName(value)` for a single field.
class _$ForecasterUpdateCWProxyImpl implements _$ForecasterUpdateCWProxy {
  const _$ForecasterUpdateCWProxyImpl(this._value);

  final ForecasterUpdate _value;

  @override
  ForecasterUpdate active(bool active) => call(active: active);

  @override
  ForecasterUpdate config(Map<String, Object>? config) => call(config: config);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecasterUpdate call({
    Object? active = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
  }) {
    return ForecasterUpdate(
      active: active == const $CopyWithPlaceholder() || active == null
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as bool,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
    );
  }
}

extension $ForecasterUpdateCopyWith on ForecasterUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecasterUpdate.copyWith(...)` or `instanceOfForecasterUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecasterUpdateCWProxy get copyWith => _$ForecasterUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecasterUpdate _$ForecasterUpdateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecasterUpdate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['active']);
      final val = ForecasterUpdate(
        active: $checkedConvert('active', (v) => v as bool),
        config: $checkedConvert(
          'config',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ForecasterUpdateToJson(ForecasterUpdate instance) =>
    <String, dynamic>{'active': instance.active, 'config': ?instance.config};
