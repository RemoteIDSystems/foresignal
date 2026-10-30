// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'list_my_editions200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ListMyEditions200ResponseCWProxy {
  ListMyEditions200Response items(List<EditionSummary> items);

  ListMyEditions200Response nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ListMyEditions200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ListMyEditions200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  ListMyEditions200Response call({
    List<EditionSummary> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfListMyEditions200Response.copyWith(...)` or call `instanceOfListMyEditions200Response.copyWith.fieldName(value)` for a single field.
class _$ListMyEditions200ResponseCWProxyImpl
    implements _$ListMyEditions200ResponseCWProxy {
  const _$ListMyEditions200ResponseCWProxyImpl(this._value);

  final ListMyEditions200Response _value;

  @override
  ListMyEditions200Response items(List<EditionSummary> items) =>
      call(items: items);

  @override
  ListMyEditions200Response nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ListMyEditions200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ListMyEditions200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ListMyEditions200Response call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ListMyEditions200Response(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<EditionSummary>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ListMyEditions200ResponseCopyWith on ListMyEditions200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfListMyEditions200Response.copyWith(...)` or `instanceOfListMyEditions200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ListMyEditions200ResponseCWProxy get copyWith =>
      _$ListMyEditions200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ListMyEditions200Response _$ListMyEditions200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ListMyEditions200Response', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = ListMyEditions200Response(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => EditionSummary.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ListMyEditions200ResponseToJson(
  ListMyEditions200Response instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
