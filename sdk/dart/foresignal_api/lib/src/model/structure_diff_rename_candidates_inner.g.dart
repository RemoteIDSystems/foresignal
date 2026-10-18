// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'structure_diff_rename_candidates_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StructureDiffRenameCandidatesInnerCWProxy {
  StructureDiffRenameCandidatesInner from(String from);

  StructureDiffRenameCandidatesInner to(String to);

  StructureDiffRenameCandidatesInner score(num score);

  StructureDiffRenameCandidatesInner reason(
    StructureDiffRenameCandidatesInnerReasonEnum reason,
  );

  StructureDiffRenameCandidatesInner readByParser(bool? readByParser);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffRenameCandidatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffRenameCandidatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StructureDiffRenameCandidatesInner call({
    String from,
    String to,
    num score,
    StructureDiffRenameCandidatesInnerReasonEnum reason,
    bool? readByParser,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStructureDiffRenameCandidatesInner.copyWith(...)` or call `instanceOfStructureDiffRenameCandidatesInner.copyWith.fieldName(value)` for a single field.
class _$StructureDiffRenameCandidatesInnerCWProxyImpl
    implements _$StructureDiffRenameCandidatesInnerCWProxy {
  const _$StructureDiffRenameCandidatesInnerCWProxyImpl(this._value);

  final StructureDiffRenameCandidatesInner _value;

  @override
  StructureDiffRenameCandidatesInner from(String from) => call(from: from);

  @override
  StructureDiffRenameCandidatesInner to(String to) => call(to: to);

  @override
  StructureDiffRenameCandidatesInner score(num score) => call(score: score);

  @override
  StructureDiffRenameCandidatesInner reason(
    StructureDiffRenameCandidatesInnerReasonEnum reason,
  ) => call(reason: reason);

  @override
  StructureDiffRenameCandidatesInner readByParser(bool? readByParser) =>
      call(readByParser: readByParser);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffRenameCandidatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffRenameCandidatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StructureDiffRenameCandidatesInner call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? readByParser = const $CopyWithPlaceholder(),
  }) {
    return StructureDiffRenameCandidatesInner(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
      score: score == const $CopyWithPlaceholder() || score == null
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as StructureDiffRenameCandidatesInnerReasonEnum,
      readByParser: readByParser == const $CopyWithPlaceholder()
          ? _value.readByParser
          // ignore: cast_nullable_to_non_nullable
          : readByParser as bool?,
    );
  }
}

extension $StructureDiffRenameCandidatesInnerCopyWith
    on StructureDiffRenameCandidatesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStructureDiffRenameCandidatesInner.copyWith(...)` or `instanceOfStructureDiffRenameCandidatesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StructureDiffRenameCandidatesInnerCWProxy get copyWith =>
      _$StructureDiffRenameCandidatesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StructureDiffRenameCandidatesInner _$StructureDiffRenameCandidatesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StructureDiffRenameCandidatesInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['from', 'to', 'score', 'reason']);
  final val = StructureDiffRenameCandidatesInner(
    from: $checkedConvert('from', (v) => v as String),
    to: $checkedConvert('to', (v) => v as String),
    score: $checkedConvert('score', (v) => v as num),
    reason: $checkedConvert(
      'reason',
      (v) =>
          $enumDecode(_$StructureDiffRenameCandidatesInnerReasonEnumEnumMap, v),
    ),
    readByParser: $checkedConvert('readByParser', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$StructureDiffRenameCandidatesInnerToJson(
  StructureDiffRenameCandidatesInner instance,
) => <String, dynamic>{
  'from': instance.from,
  'to': instance.to,
  'score': instance.score,
  'reason':
      _$StructureDiffRenameCandidatesInnerReasonEnumEnumMap[instance.reason]!,
  'readByParser': ?instance.readByParser,
};

const _$StructureDiffRenameCandidatesInnerReasonEnumEnumMap = {
  StructureDiffRenameCandidatesInnerReasonEnum.nameSimilarity:
      'name-similarity',
  StructureDiffRenameCandidatesInnerReasonEnum.snakeCamel: 'snake-camel',
  StructureDiffRenameCandidatesInnerReasonEnum.valueDistribution:
      'value-distribution',
};
