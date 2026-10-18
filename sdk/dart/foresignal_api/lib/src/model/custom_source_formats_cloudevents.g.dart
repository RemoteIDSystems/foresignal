// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats_cloudevents.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsCloudeventsCWProxy {
  CustomSourceFormatsCloudevents typeKinds(Map<String, String>? typeKinds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsCloudevents(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsCloudevents(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormatsCloudevents call({Map<String, String>? typeKinds});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormatsCloudevents.copyWith(...)` or call `instanceOfCustomSourceFormatsCloudevents.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsCloudeventsCWProxyImpl
    implements _$CustomSourceFormatsCloudeventsCWProxy {
  const _$CustomSourceFormatsCloudeventsCWProxyImpl(this._value);

  final CustomSourceFormatsCloudevents _value;

  @override
  CustomSourceFormatsCloudevents typeKinds(Map<String, String>? typeKinds) =>
      call(typeKinds: typeKinds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsCloudevents(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsCloudevents(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormatsCloudevents call({
    Object? typeKinds = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormatsCloudevents(
      typeKinds: typeKinds == const $CopyWithPlaceholder()
          ? _value.typeKinds
          // ignore: cast_nullable_to_non_nullable
          : typeKinds as Map<String, String>?,
    );
  }
}

extension $CustomSourceFormatsCloudeventsCopyWith
    on CustomSourceFormatsCloudevents {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormatsCloudevents.copyWith(...)` or `instanceOfCustomSourceFormatsCloudevents.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsCloudeventsCWProxy get copyWith =>
      _$CustomSourceFormatsCloudeventsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormatsCloudevents _$CustomSourceFormatsCloudeventsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormatsCloudevents', json, ($checkedConvert) {
  final val = CustomSourceFormatsCloudevents(
    typeKinds: $checkedConvert(
      'typeKinds',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
  );
  return val;
});

Map<String, dynamic> _$CustomSourceFormatsCloudeventsToJson(
  CustomSourceFormatsCloudevents instance,
) => <String, dynamic>{'typeKinds': ?instance.typeKinds};
