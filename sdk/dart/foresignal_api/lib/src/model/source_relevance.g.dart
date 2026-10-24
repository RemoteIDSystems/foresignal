// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_relevance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceRelevanceCWProxy {
  SourceRelevance sourceId(String sourceId);

  SourceRelevance name(String name);

  SourceRelevance kinds(List<String> kinds);

  SourceRelevance verdict(SourceRelevanceVerdictEnum verdict);

  SourceRelevance reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevance(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceRelevance call({
    String sourceId,
    String name,
    List<String> kinds,
    SourceRelevanceVerdictEnum verdict,
    String reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceRelevance.copyWith(...)` or call `instanceOfSourceRelevance.copyWith.fieldName(value)` for a single field.
class _$SourceRelevanceCWProxyImpl implements _$SourceRelevanceCWProxy {
  const _$SourceRelevanceCWProxyImpl(this._value);

  final SourceRelevance _value;

  @override
  SourceRelevance sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  SourceRelevance name(String name) => call(name: name);

  @override
  SourceRelevance kinds(List<String> kinds) => call(kinds: kinds);

  @override
  SourceRelevance verdict(SourceRelevanceVerdictEnum verdict) =>
      call(verdict: verdict);

  @override
  SourceRelevance reason(String reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceRelevance call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? verdict = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return SourceRelevance(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      kinds: kinds == const $CopyWithPlaceholder() || kinds == null
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>,
      verdict: verdict == const $CopyWithPlaceholder() || verdict == null
          ? _value.verdict
          // ignore: cast_nullable_to_non_nullable
          : verdict as SourceRelevanceVerdictEnum,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $SourceRelevanceCopyWith on SourceRelevance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceRelevance.copyWith(...)` or `instanceOfSourceRelevance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceRelevanceCWProxy get copyWith => _$SourceRelevanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceRelevance _$SourceRelevanceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SourceRelevance', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['sourceId', 'name', 'kinds', 'verdict', 'reason'],
      );
      final val = SourceRelevance(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        verdict: $checkedConvert(
          'verdict',
          (v) => $enumDecode(_$SourceRelevanceVerdictEnumEnumMap, v),
        ),
        reason: $checkedConvert('reason', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SourceRelevanceToJson(SourceRelevance instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'name': instance.name,
      'kinds': instance.kinds,
      'verdict': _$SourceRelevanceVerdictEnumEnumMap[instance.verdict]!,
      'reason': instance.reason,
    };

const _$SourceRelevanceVerdictEnumEnumMap = {
  SourceRelevanceVerdictEnum.relevant: 'relevant',
  SourceRelevanceVerdictEnum.marginal: 'marginal',
  SourceRelevanceVerdictEnum.notMeaningful: 'not-meaningful',
  SourceRelevanceVerdictEnum.unrated: 'unrated',
};
