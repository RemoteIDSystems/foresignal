// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resource_thresholds_source_storm.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResourceThresholdsSourceStormCWProxy {
  ResourceThresholdsSourceStorm failedSources(int? failedSources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThresholdsSourceStorm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThresholdsSourceStorm(...).copyWith(id: 12, name: "My name")
  /// ```
  ResourceThresholdsSourceStorm call({int? failedSources});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResourceThresholdsSourceStorm.copyWith(...)` or call `instanceOfResourceThresholdsSourceStorm.copyWith.fieldName(value)` for a single field.
class _$ResourceThresholdsSourceStormCWProxyImpl
    implements _$ResourceThresholdsSourceStormCWProxy {
  const _$ResourceThresholdsSourceStormCWProxyImpl(this._value);

  final ResourceThresholdsSourceStorm _value;

  @override
  ResourceThresholdsSourceStorm failedSources(int? failedSources) =>
      call(failedSources: failedSources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThresholdsSourceStorm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThresholdsSourceStorm(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResourceThresholdsSourceStorm call({
    Object? failedSources = const $CopyWithPlaceholder(),
  }) {
    return ResourceThresholdsSourceStorm(
      failedSources: failedSources == const $CopyWithPlaceholder()
          ? _value.failedSources
          // ignore: cast_nullable_to_non_nullable
          : failedSources as int?,
    );
  }
}

extension $ResourceThresholdsSourceStormCopyWith
    on ResourceThresholdsSourceStorm {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResourceThresholdsSourceStorm.copyWith(...)` or `instanceOfResourceThresholdsSourceStorm.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResourceThresholdsSourceStormCWProxy get copyWith =>
      _$ResourceThresholdsSourceStormCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResourceThresholdsSourceStorm _$ResourceThresholdsSourceStormFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ResourceThresholdsSourceStorm', json, ($checkedConvert) {
  final val = ResourceThresholdsSourceStorm(
    failedSources: $checkedConvert(
      'failedSources',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ResourceThresholdsSourceStormToJson(
  ResourceThresholdsSourceStorm instance,
) => <String, dynamic>{'failedSources': ?instance.failedSources};
