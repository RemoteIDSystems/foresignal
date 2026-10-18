// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'search200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$Search200ResponseCWProxy {
  Search200Response mode(Search200ResponseModeEnum mode);

  Search200Response items(List<SearchResult> items);

  Search200Response degraded(bool? degraded);

  Search200Response attributions(List<Attribution>? attributions);

  Search200Response licenceFiltered(int? licenceFiltered);

  Search200Response reason(String? reason);

  Search200Response count(int count);

  Search200Response exact(bool exact);

  Search200Response watermarkSeq(int watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Search200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Search200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  Search200Response call({
    Search200ResponseModeEnum mode,
    List<SearchResult> items,
    bool? degraded,
    List<Attribution>? attributions,
    int? licenceFiltered,
    String? reason,
    int count,
    bool exact,
    int watermarkSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSearch200Response.copyWith(...)` or call `instanceOfSearch200Response.copyWith.fieldName(value)` for a single field.
class _$Search200ResponseCWProxyImpl implements _$Search200ResponseCWProxy {
  const _$Search200ResponseCWProxyImpl(this._value);

  final Search200Response _value;

  @override
  Search200Response mode(Search200ResponseModeEnum mode) => call(mode: mode);

  @override
  Search200Response items(List<SearchResult> items) => call(items: items);

  @override
  Search200Response degraded(bool? degraded) => call(degraded: degraded);

  @override
  Search200Response attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  Search200Response licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  @override
  Search200Response reason(String? reason) => call(reason: reason);

  @override
  Search200Response count(int count) => call(count: count);

  @override
  Search200Response exact(bool exact) => call(exact: exact);

  @override
  Search200Response watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Search200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Search200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Search200Response call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? degraded = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? exact = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
  }) {
    return Search200Response(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as Search200ResponseModeEnum,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SearchResult>,
      degraded: degraded == const $CopyWithPlaceholder()
          ? _value.degraded
          // ignore: cast_nullable_to_non_nullable
          : degraded as bool?,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>?,
      licenceFiltered: licenceFiltered == const $CopyWithPlaceholder()
          ? _value.licenceFiltered
          // ignore: cast_nullable_to_non_nullable
          : licenceFiltered as int?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      exact: exact == const $CopyWithPlaceholder() || exact == null
          ? _value.exact
          // ignore: cast_nullable_to_non_nullable
          : exact as bool,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
    );
  }
}

extension $Search200ResponseCopyWith on Search200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSearch200Response.copyWith(...)` or `instanceOfSearch200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$Search200ResponseCWProxy get copyWith =>
      _$Search200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Search200Response _$Search200ResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Search200Response', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['mode', 'items', 'count', 'exact', 'watermarkSeq'],
      );
      final val = Search200Response(
        mode: $checkedConvert(
          'mode',
          (v) => $enumDecode(_$Search200ResponseModeEnumEnumMap, v),
        ),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SearchResult.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        degraded: $checkedConvert('degraded', (v) => v as bool?),
        attributions: $checkedConvert(
          'attributions',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        licenceFiltered: $checkedConvert(
          'licenceFiltered',
          (v) => (v as num?)?.toInt(),
        ),
        reason: $checkedConvert('reason', (v) => v as String?),
        count: $checkedConvert('count', (v) => (v as num).toInt()),
        exact: $checkedConvert('exact', (v) => v as bool),
        watermarkSeq: $checkedConvert(
          'watermarkSeq',
          (v) => (v as num).toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$Search200ResponseToJson(Search200Response instance) =>
    <String, dynamic>{
      'mode': _$Search200ResponseModeEnumEnumMap[instance.mode]!,
      'items': instance.items.map((e) => e.toJson()).toList(),
      'degraded': ?instance.degraded,
      'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
      'licenceFiltered': ?instance.licenceFiltered,
      'reason': ?instance.reason,
      'count': instance.count,
      'exact': instance.exact,
      'watermarkSeq': instance.watermarkSeq,
    };

const _$Search200ResponseModeEnumEnumMap = {
  Search200ResponseModeEnum.hybrid: 'hybrid',
  Search200ResponseModeEnum.semantic: 'semantic',
  Search200ResponseModeEnum.text: 'text',
};
