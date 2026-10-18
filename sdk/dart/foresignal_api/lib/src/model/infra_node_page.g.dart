// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_node_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraNodePageCWProxy {
  InfraNodePage items(List<InfraNode> items);

  InfraNodePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraNodePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraNodePage(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraNodePage call({List<InfraNode> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraNodePage.copyWith(...)` or call `instanceOfInfraNodePage.copyWith.fieldName(value)` for a single field.
class _$InfraNodePageCWProxyImpl implements _$InfraNodePageCWProxy {
  const _$InfraNodePageCWProxyImpl(this._value);

  final InfraNodePage _value;

  @override
  InfraNodePage items(List<InfraNode> items) => call(items: items);

  @override
  InfraNodePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraNodePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraNodePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraNodePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return InfraNodePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<InfraNode>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $InfraNodePageCopyWith on InfraNodePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraNodePage.copyWith(...)` or `instanceOfInfraNodePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraNodePageCWProxy get copyWith => _$InfraNodePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraNodePage _$InfraNodePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraNodePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = InfraNodePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => InfraNode.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$InfraNodePageToJson(InfraNodePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
