// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'json_feed_authors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JsonFeedAuthorsInnerCWProxy {
  JsonFeedAuthorsInner name(String? name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedAuthorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedAuthorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  JsonFeedAuthorsInner call({String? name});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJsonFeedAuthorsInner.copyWith(...)` or call `instanceOfJsonFeedAuthorsInner.copyWith.fieldName(value)` for a single field.
class _$JsonFeedAuthorsInnerCWProxyImpl
    implements _$JsonFeedAuthorsInnerCWProxy {
  const _$JsonFeedAuthorsInnerCWProxyImpl(this._value);

  final JsonFeedAuthorsInner _value;

  @override
  JsonFeedAuthorsInner name(String? name) => call(name: name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedAuthorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedAuthorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  JsonFeedAuthorsInner call({Object? name = const $CopyWithPlaceholder()}) {
    return JsonFeedAuthorsInner(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
    );
  }
}

extension $JsonFeedAuthorsInnerCopyWith on JsonFeedAuthorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJsonFeedAuthorsInner.copyWith(...)` or `instanceOfJsonFeedAuthorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JsonFeedAuthorsInnerCWProxy get copyWith =>
      _$JsonFeedAuthorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

JsonFeedAuthorsInner _$JsonFeedAuthorsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('JsonFeedAuthorsInner', json, ($checkedConvert) {
  final val = JsonFeedAuthorsInner(
    name: $checkedConvert('name', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$JsonFeedAuthorsInnerToJson(
  JsonFeedAuthorsInner instance,
) => <String, dynamic>{'name': ?instance.name};
