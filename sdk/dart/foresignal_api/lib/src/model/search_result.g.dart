// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'search_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SearchResultCWProxy {
  SearchResult docType(SearchResultDocTypeEnum docType);

  SearchResult docId(String docId);

  SearchResult score(num score);

  SearchResult title(String title);

  SearchResult snippet(String? snippet);

  SearchResult observedAt(DateTime? observedAt);

  SearchResult source_(String? source_);

  SearchResult kind(String? kind);

  SearchResult geometry(Geometry? geometry);

  SearchResult record(Record? record);

  SearchResult seq(int? seq);

  SearchResult effectiveTime(DateTime? effectiveTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  SearchResult call({
    SearchResultDocTypeEnum docType,
    String docId,
    num score,
    String title,
    String? snippet,
    DateTime? observedAt,
    String? source_,
    String? kind,
    Geometry? geometry,
    Record? record,
    int? seq,
    DateTime? effectiveTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSearchResult.copyWith(...)` or call `instanceOfSearchResult.copyWith.fieldName(value)` for a single field.
class _$SearchResultCWProxyImpl implements _$SearchResultCWProxy {
  const _$SearchResultCWProxyImpl(this._value);

  final SearchResult _value;

  @override
  SearchResult docType(SearchResultDocTypeEnum docType) =>
      call(docType: docType);

  @override
  SearchResult docId(String docId) => call(docId: docId);

  @override
  SearchResult score(num score) => call(score: score);

  @override
  SearchResult title(String title) => call(title: title);

  @override
  SearchResult snippet(String? snippet) => call(snippet: snippet);

  @override
  SearchResult observedAt(DateTime? observedAt) => call(observedAt: observedAt);

  @override
  SearchResult source_(String? source_) => call(source_: source_);

  @override
  SearchResult kind(String? kind) => call(kind: kind);

  @override
  SearchResult geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  SearchResult record(Record? record) => call(record: record);

  @override
  SearchResult seq(int? seq) => call(seq: seq);

  @override
  SearchResult effectiveTime(DateTime? effectiveTime) =>
      call(effectiveTime: effectiveTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SearchResult call({
    Object? docType = const $CopyWithPlaceholder(),
    Object? docId = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? snippet = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? record = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? effectiveTime = const $CopyWithPlaceholder(),
  }) {
    return SearchResult(
      docType: docType == const $CopyWithPlaceholder() || docType == null
          ? _value.docType
          // ignore: cast_nullable_to_non_nullable
          : docType as SearchResultDocTypeEnum,
      docId: docId == const $CopyWithPlaceholder() || docId == null
          ? _value.docId
          // ignore: cast_nullable_to_non_nullable
          : docId as String,
      score: score == const $CopyWithPlaceholder() || score == null
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      snippet: snippet == const $CopyWithPlaceholder()
          ? _value.snippet
          // ignore: cast_nullable_to_non_nullable
          : snippet as String?,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as Record?,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      effectiveTime: effectiveTime == const $CopyWithPlaceholder()
          ? _value.effectiveTime
          // ignore: cast_nullable_to_non_nullable
          : effectiveTime as DateTime?,
    );
  }
}

extension $SearchResultCopyWith on SearchResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSearchResult.copyWith(...)` or `instanceOfSearchResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SearchResultCWProxy get copyWith => _$SearchResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SearchResult _$SearchResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SearchResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['docType', 'docId', 'score', 'title'],
      );
      final val = SearchResult(
        docType: $checkedConvert(
          'docType',
          (v) => $enumDecode(_$SearchResultDocTypeEnumEnumMap, v),
        ),
        docId: $checkedConvert('docId', (v) => v as String),
        score: $checkedConvert('score', (v) => v as num),
        title: $checkedConvert('title', (v) => v as String),
        snippet: $checkedConvert('snippet', (v) => v as String?),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        source_: $checkedConvert('source', (v) => v as String?),
        kind: $checkedConvert('kind', (v) => v as String?),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        record: $checkedConvert(
          'record',
          (v) => v == null ? null : Record.fromJson(v as Map<String, dynamic>),
        ),
        seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
        effectiveTime: $checkedConvert(
          'effectiveTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$SearchResultToJson(SearchResult instance) =>
    <String, dynamic>{
      'docType': _$SearchResultDocTypeEnumEnumMap[instance.docType]!,
      'docId': instance.docId,
      'score': instance.score,
      'title': instance.title,
      'snippet': ?instance.snippet,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'source': ?instance.source_,
      'kind': ?instance.kind,
      'geometry': ?instance.geometry?.toJson(),
      'record': ?instance.record?.toJson(),
      'seq': ?instance.seq,
      'effectiveTime': ?instance.effectiveTime?.toIso8601String(),
    };

const _$SearchResultDocTypeEnumEnumMap = {
  SearchResultDocTypeEnum.record: 'record',
  SearchResultDocTypeEnum.briefing: 'briefing',
  SearchResultDocTypeEnum.alert: 'alert',
};
