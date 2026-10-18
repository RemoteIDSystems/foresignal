// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_item_rejection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpItemRejectionCWProxy {
  EdpItemRejection index(int index);

  EdpItemRejection stream(String? stream);

  EdpItemRejection id(String? id);

  EdpItemRejection reason(String reason);

  EdpItemRejection schemaErrors(
    List<EdpItemRejectionSchemaErrorsInner>? schemaErrors,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpItemRejection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpItemRejection(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpItemRejection call({
    int index,
    String? stream,
    String? id,
    String reason,
    List<EdpItemRejectionSchemaErrorsInner>? schemaErrors,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpItemRejection.copyWith(...)` or call `instanceOfEdpItemRejection.copyWith.fieldName(value)` for a single field.
class _$EdpItemRejectionCWProxyImpl implements _$EdpItemRejectionCWProxy {
  const _$EdpItemRejectionCWProxyImpl(this._value);

  final EdpItemRejection _value;

  @override
  EdpItemRejection index(int index) => call(index: index);

  @override
  EdpItemRejection stream(String? stream) => call(stream: stream);

  @override
  EdpItemRejection id(String? id) => call(id: id);

  @override
  EdpItemRejection reason(String reason) => call(reason: reason);

  @override
  EdpItemRejection schemaErrors(
    List<EdpItemRejectionSchemaErrorsInner>? schemaErrors,
  ) => call(schemaErrors: schemaErrors);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpItemRejection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpItemRejection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpItemRejection call({
    Object? index = const $CopyWithPlaceholder(),
    Object? stream = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? schemaErrors = const $CopyWithPlaceholder(),
  }) {
    return EdpItemRejection(
      index: index == const $CopyWithPlaceholder() || index == null
          ? _value.index
          // ignore: cast_nullable_to_non_nullable
          : index as int,
      stream: stream == const $CopyWithPlaceholder()
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String?,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      schemaErrors: schemaErrors == const $CopyWithPlaceholder()
          ? _value.schemaErrors
          // ignore: cast_nullable_to_non_nullable
          : schemaErrors as List<EdpItemRejectionSchemaErrorsInner>?,
    );
  }
}

extension $EdpItemRejectionCopyWith on EdpItemRejection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpItemRejection.copyWith(...)` or `instanceOfEdpItemRejection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpItemRejectionCWProxy get copyWith => _$EdpItemRejectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpItemRejection _$EdpItemRejectionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpItemRejection', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['index', 'reason']);
      final val = EdpItemRejection(
        index: $checkedConvert('index', (v) => (v as num).toInt()),
        stream: $checkedConvert('stream', (v) => v as String?),
        id: $checkedConvert('id', (v) => v as String?),
        reason: $checkedConvert('reason', (v) => v as String),
        schemaErrors: $checkedConvert(
          'schemaErrors',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => EdpItemRejectionSchemaErrorsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpItemRejectionToJson(EdpItemRejection instance) =>
    <String, dynamic>{
      'index': instance.index,
      'stream': ?instance.stream,
      'id': ?instance.id,
      'reason': instance.reason,
      'schemaErrors': ?instance.schemaErrors?.map((e) => e.toJson()).toList(),
    };
