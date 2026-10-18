// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'index_weights_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndexWeightsInputCWProxy {
  IndexWeightsInput source_(IndexWeightsInputSource_Enum source_);

  IndexWeightsInput version(int? version);

  IndexWeightsInput weights(Map<String, num>? weights);

  IndexWeightsInput rationale(String? rationale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexWeightsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexWeightsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  IndexWeightsInput call({
    IndexWeightsInputSource_Enum source_,
    int? version,
    Map<String, num>? weights,
    String? rationale,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndexWeightsInput.copyWith(...)` or call `instanceOfIndexWeightsInput.copyWith.fieldName(value)` for a single field.
class _$IndexWeightsInputCWProxyImpl implements _$IndexWeightsInputCWProxy {
  const _$IndexWeightsInputCWProxyImpl(this._value);

  final IndexWeightsInput _value;

  @override
  IndexWeightsInput source_(IndexWeightsInputSource_Enum source_) =>
      call(source_: source_);

  @override
  IndexWeightsInput version(int? version) => call(version: version);

  @override
  IndexWeightsInput weights(Map<String, num>? weights) =>
      call(weights: weights);

  @override
  IndexWeightsInput rationale(String? rationale) => call(rationale: rationale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexWeightsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexWeightsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndexWeightsInput call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? weights = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
  }) {
    return IndexWeightsInput(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as IndexWeightsInputSource_Enum,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
      weights: weights == const $CopyWithPlaceholder()
          ? _value.weights
          // ignore: cast_nullable_to_non_nullable
          : weights as Map<String, num>?,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
    );
  }
}

extension $IndexWeightsInputCopyWith on IndexWeightsInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndexWeightsInput.copyWith(...)` or `instanceOfIndexWeightsInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndexWeightsInputCWProxy get copyWith =>
      _$IndexWeightsInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndexWeightsInput _$IndexWeightsInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IndexWeightsInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['source']);
      final val = IndexWeightsInput(
        source_: $checkedConvert(
          'source',
          (v) => $enumDecode(_$IndexWeightsInputSource_EnumEnumMap, v),
        ),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
        weights: $checkedConvert(
          'weights',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as num),
          ),
        ),
        rationale: $checkedConvert('rationale', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$IndexWeightsInputToJson(IndexWeightsInput instance) =>
    <String, dynamic>{
      'source': _$IndexWeightsInputSource_EnumEnumMap[instance.source_]!,
      'version': ?instance.version,
      'weights': ?instance.weights,
      'rationale': ?instance.rationale,
    };

const _$IndexWeightsInputSource_EnumEnumMap = {
  IndexWeightsInputSource_Enum.fitted: 'fitted',
  IndexWeightsInputSource_Enum.operator_: 'operator',
  IndexWeightsInputSource_Enum.default_: 'default',
};
