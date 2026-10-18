// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'json_feed_item_authors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JsonFeedItemAuthorsInnerCWProxy {
  JsonFeedItemAuthorsInner name(String? name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedItemAuthorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedItemAuthorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  JsonFeedItemAuthorsInner call({String? name});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJsonFeedItemAuthorsInner.copyWith(...)` or call `instanceOfJsonFeedItemAuthorsInner.copyWith.fieldName(value)` for a single field.
class _$JsonFeedItemAuthorsInnerCWProxyImpl
    implements _$JsonFeedItemAuthorsInnerCWProxy {
  const _$JsonFeedItemAuthorsInnerCWProxyImpl(this._value);

  final JsonFeedItemAuthorsInner _value;

  @override
  JsonFeedItemAuthorsInner name(String? name) => call(name: name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedItemAuthorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedItemAuthorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  JsonFeedItemAuthorsInner call({Object? name = const $CopyWithPlaceholder()}) {
    return JsonFeedItemAuthorsInner(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
    );
  }
}

extension $JsonFeedItemAuthorsInnerCopyWith on JsonFeedItemAuthorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJsonFeedItemAuthorsInner.copyWith(...)` or `instanceOfJsonFeedItemAuthorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JsonFeedItemAuthorsInnerCWProxy get copyWith =>
      _$JsonFeedItemAuthorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

JsonFeedItemAuthorsInner _$JsonFeedItemAuthorsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('JsonFeedItemAuthorsInner', json, ($checkedConvert) {
  final val = JsonFeedItemAuthorsInner(
    name: $checkedConvert('name', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$JsonFeedItemAuthorsInnerToJson(
  JsonFeedItemAuthorsInner instance,
) => <String, dynamic>{'name': ?instance.name};
