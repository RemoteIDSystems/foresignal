// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_scope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskScopeCWProxy {
  AskScope sources(List<String>? sources);

  AskScope kinds(List<String>? kinds);

  AskScope bbox(String? bbox);

  AskScope since(DateTime? since);

  AskScope until(DateTime? until);

  AskScope placeIds(List<String>? placeIds);

  AskScope viewId(String? viewId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskScope(...).copyWith(id: 12, name: "My name")
  /// ```
  AskScope call({
    List<String>? sources,
    List<String>? kinds,
    String? bbox,
    DateTime? since,
    DateTime? until,
    List<String>? placeIds,
    String? viewId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskScope.copyWith(...)` or call `instanceOfAskScope.copyWith.fieldName(value)` for a single field.
class _$AskScopeCWProxyImpl implements _$AskScopeCWProxy {
  const _$AskScopeCWProxyImpl(this._value);

  final AskScope _value;

  @override
  AskScope sources(List<String>? sources) => call(sources: sources);

  @override
  AskScope kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  AskScope bbox(String? bbox) => call(bbox: bbox);

  @override
  AskScope since(DateTime? since) => call(since: since);

  @override
  AskScope until(DateTime? until) => call(until: until);

  @override
  AskScope placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  AskScope viewId(String? viewId) => call(viewId: viewId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskScope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskScope call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? until = const $CopyWithPlaceholder(),
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? viewId = const $CopyWithPlaceholder(),
  }) {
    return AskScope(
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as String?,
      since: since == const $CopyWithPlaceholder()
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime?,
      until: until == const $CopyWithPlaceholder()
          ? _value.until
          // ignore: cast_nullable_to_non_nullable
          : until as DateTime?,
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      viewId: viewId == const $CopyWithPlaceholder()
          ? _value.viewId
          // ignore: cast_nullable_to_non_nullable
          : viewId as String?,
    );
  }
}

extension $AskScopeCopyWith on AskScope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskScope.copyWith(...)` or `instanceOfAskScope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskScopeCWProxy get copyWith => _$AskScopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskScope _$AskScopeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskScope', json, ($checkedConvert) {
      final val = AskScope(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bbox: $checkedConvert('bbox', (v) => v as String?),
        since: $checkedConvert(
          'since',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        until: $checkedConvert(
          'until',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        placeIds: $checkedConvert(
          'placeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        viewId: $checkedConvert('viewId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AskScopeToJson(AskScope instance) => <String, dynamic>{
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'bbox': ?instance.bbox,
  'since': ?instance.since?.toIso8601String(),
  'until': ?instance.until?.toIso8601String(),
  'placeIds': ?instance.placeIds,
  'viewId': ?instance.viewId,
};
