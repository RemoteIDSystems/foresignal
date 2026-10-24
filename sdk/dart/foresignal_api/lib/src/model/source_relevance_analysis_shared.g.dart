// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_relevance_analysis_shared.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceRelevanceAnalysisSharedCWProxy {
  SourceRelevanceAnalysisShared kinds(List<String> kinds);

  SourceRelevanceAnalysisShared entityTypes(List<String> entityTypes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceAnalysisShared(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceAnalysisShared(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceRelevanceAnalysisShared call({
    List<String> kinds,
    List<String> entityTypes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceRelevanceAnalysisShared.copyWith(...)` or call `instanceOfSourceRelevanceAnalysisShared.copyWith.fieldName(value)` for a single field.
class _$SourceRelevanceAnalysisSharedCWProxyImpl
    implements _$SourceRelevanceAnalysisSharedCWProxy {
  const _$SourceRelevanceAnalysisSharedCWProxyImpl(this._value);

  final SourceRelevanceAnalysisShared _value;

  @override
  SourceRelevanceAnalysisShared kinds(List<String> kinds) => call(kinds: kinds);

  @override
  SourceRelevanceAnalysisShared entityTypes(List<String> entityTypes) =>
      call(entityTypes: entityTypes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceAnalysisShared(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceAnalysisShared(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceRelevanceAnalysisShared call({
    Object? kinds = const $CopyWithPlaceholder(),
    Object? entityTypes = const $CopyWithPlaceholder(),
  }) {
    return SourceRelevanceAnalysisShared(
      kinds: kinds == const $CopyWithPlaceholder() || kinds == null
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>,
      entityTypes:
          entityTypes == const $CopyWithPlaceholder() || entityTypes == null
          ? _value.entityTypes
          // ignore: cast_nullable_to_non_nullable
          : entityTypes as List<String>,
    );
  }
}

extension $SourceRelevanceAnalysisSharedCopyWith
    on SourceRelevanceAnalysisShared {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceRelevanceAnalysisShared.copyWith(...)` or `instanceOfSourceRelevanceAnalysisShared.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceRelevanceAnalysisSharedCWProxy get copyWith =>
      _$SourceRelevanceAnalysisSharedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceRelevanceAnalysisShared _$SourceRelevanceAnalysisSharedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceRelevanceAnalysisShared', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['kinds', 'entityTypes']);
  final val = SourceRelevanceAnalysisShared(
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    entityTypes: $checkedConvert(
      'entityTypes',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceRelevanceAnalysisSharedToJson(
  SourceRelevanceAnalysisShared instance,
) => <String, dynamic>{
  'kinds': instance.kinds,
  'entityTypes': instance.entityTypes,
};
