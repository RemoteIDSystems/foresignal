// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'kind_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KindSummaryCWProxy {
  KindSummary kind(String kind);

  KindSummary currentCount(int currentCount);

  KindSummary sources(List<String> sources);

  KindSummary display(KindDisplay? display);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KindSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KindSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  KindSummary call({
    String kind,
    int currentCount,
    List<String> sources,
    KindDisplay? display,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKindSummary.copyWith(...)` or call `instanceOfKindSummary.copyWith.fieldName(value)` for a single field.
class _$KindSummaryCWProxyImpl implements _$KindSummaryCWProxy {
  const _$KindSummaryCWProxyImpl(this._value);

  final KindSummary _value;

  @override
  KindSummary kind(String kind) => call(kind: kind);

  @override
  KindSummary currentCount(int currentCount) =>
      call(currentCount: currentCount);

  @override
  KindSummary sources(List<String> sources) => call(sources: sources);

  @override
  KindSummary display(KindDisplay? display) => call(display: display);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KindSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KindSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KindSummary call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? currentCount = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? display = const $CopyWithPlaceholder(),
  }) {
    return KindSummary(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      currentCount:
          currentCount == const $CopyWithPlaceholder() || currentCount == null
          ? _value.currentCount
          // ignore: cast_nullable_to_non_nullable
          : currentCount as int,
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>,
      display: display == const $CopyWithPlaceholder()
          ? _value.display
          // ignore: cast_nullable_to_non_nullable
          : display as KindDisplay?,
    );
  }
}

extension $KindSummaryCopyWith on KindSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKindSummary.copyWith(...)` or `instanceOfKindSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KindSummaryCWProxy get copyWith => _$KindSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KindSummary _$KindSummaryFromJson(Map<String, dynamic> json) => $checkedCreate(
  'KindSummary',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['kind', 'currentCount', 'sources']);
    final val = KindSummary(
      kind: $checkedConvert('kind', (v) => v as String),
      currentCount: $checkedConvert('currentCount', (v) => (v as num).toInt()),
      sources: $checkedConvert(
        'sources',
        (v) => (v as List<dynamic>).map((e) => e as String).toList(),
      ),
      display: $checkedConvert(
        'display',
        (v) =>
            v == null ? null : KindDisplay.fromJson(v as Map<String, dynamic>),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$KindSummaryToJson(KindSummary instance) =>
    <String, dynamic>{
      'kind': instance.kind,
      'currentCount': instance.currentCount,
      'sources': instance.sources,
      'display': ?instance.display?.toJson(),
    };
