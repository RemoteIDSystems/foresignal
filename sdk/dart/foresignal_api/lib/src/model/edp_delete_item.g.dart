// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_delete_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpDeleteItemCWProxy {
  EdpDeleteItem stream(String stream);

  EdpDeleteItem type(EdpDeleteItemTypeEnum type);

  EdpDeleteItem id(String id);

  EdpDeleteItem observedAt(DateTime? observedAt);

  EdpDeleteItem properties(Map<String, Object>? properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDeleteItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDeleteItem(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpDeleteItem call({
    String stream,
    EdpDeleteItemTypeEnum type,
    String id,
    DateTime? observedAt,
    Map<String, Object>? properties,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpDeleteItem.copyWith(...)` or call `instanceOfEdpDeleteItem.copyWith.fieldName(value)` for a single field.
class _$EdpDeleteItemCWProxyImpl implements _$EdpDeleteItemCWProxy {
  const _$EdpDeleteItemCWProxyImpl(this._value);

  final EdpDeleteItem _value;

  @override
  EdpDeleteItem stream(String stream) => call(stream: stream);

  @override
  EdpDeleteItem type(EdpDeleteItemTypeEnum type) => call(type: type);

  @override
  EdpDeleteItem id(String id) => call(id: id);

  @override
  EdpDeleteItem observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  EdpDeleteItem properties(Map<String, Object>? properties) =>
      call(properties: properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDeleteItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDeleteItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpDeleteItem call({
    Object? stream = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
  }) {
    return EdpDeleteItem(
      stream: stream == const $CopyWithPlaceholder() || stream == null
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpDeleteItemTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      properties: properties == const $CopyWithPlaceholder()
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Object>?,
    );
  }
}

extension $EdpDeleteItemCopyWith on EdpDeleteItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpDeleteItem.copyWith(...)` or `instanceOfEdpDeleteItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpDeleteItemCWProxy get copyWith => _$EdpDeleteItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpDeleteItem _$EdpDeleteItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpDeleteItem', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['stream', 'type', 'id']);
      final val = EdpDeleteItem(
        stream: $checkedConvert('stream', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$EdpDeleteItemTypeEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        properties: $checkedConvert(
          'properties',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpDeleteItemToJson(EdpDeleteItem instance) =>
    <String, dynamic>{
      'stream': instance.stream,
      'type': _$EdpDeleteItemTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'properties': ?instance.properties,
    };

const _$EdpDeleteItemTypeEnumEnumMap = {EdpDeleteItemTypeEnum.delete: 'delete'};
