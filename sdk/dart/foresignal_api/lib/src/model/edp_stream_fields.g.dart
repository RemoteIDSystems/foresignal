// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_stream_fields.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpStreamFieldsCWProxy {
  EdpStreamFields title(String? title);

  EdpStreamFields significant(List<String>? significant);

  EdpStreamFields volatile(List<String>? volatile);

  EdpStreamFields observedAtMode(
    EdpStreamFieldsObservedAtModeEnum? observedAtMode,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpStreamFields(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpStreamFields(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpStreamFields call({
    String? title,
    List<String>? significant,
    List<String>? volatile,
    EdpStreamFieldsObservedAtModeEnum? observedAtMode,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpStreamFields.copyWith(...)` or call `instanceOfEdpStreamFields.copyWith.fieldName(value)` for a single field.
class _$EdpStreamFieldsCWProxyImpl implements _$EdpStreamFieldsCWProxy {
  const _$EdpStreamFieldsCWProxyImpl(this._value);

  final EdpStreamFields _value;

  @override
  EdpStreamFields title(String? title) => call(title: title);

  @override
  EdpStreamFields significant(List<String>? significant) =>
      call(significant: significant);

  @override
  EdpStreamFields volatile(List<String>? volatile) => call(volatile: volatile);

  @override
  EdpStreamFields observedAtMode(
    EdpStreamFieldsObservedAtModeEnum? observedAtMode,
  ) => call(observedAtMode: observedAtMode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpStreamFields(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpStreamFields(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpStreamFields call({
    Object? title = const $CopyWithPlaceholder(),
    Object? significant = const $CopyWithPlaceholder(),
    Object? volatile = const $CopyWithPlaceholder(),
    Object? observedAtMode = const $CopyWithPlaceholder(),
  }) {
    return EdpStreamFields(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      significant: significant == const $CopyWithPlaceholder()
          ? _value.significant
          // ignore: cast_nullable_to_non_nullable
          : significant as List<String>?,
      volatile: volatile == const $CopyWithPlaceholder()
          ? _value.volatile
          // ignore: cast_nullable_to_non_nullable
          : volatile as List<String>?,
      observedAtMode: observedAtMode == const $CopyWithPlaceholder()
          ? _value.observedAtMode
          // ignore: cast_nullable_to_non_nullable
          : observedAtMode as EdpStreamFieldsObservedAtModeEnum?,
    );
  }
}

extension $EdpStreamFieldsCopyWith on EdpStreamFields {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpStreamFields.copyWith(...)` or `instanceOfEdpStreamFields.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpStreamFieldsCWProxy get copyWith => _$EdpStreamFieldsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpStreamFields _$EdpStreamFieldsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpStreamFields', json, ($checkedConvert) {
      final val = EdpStreamFields(
        title: $checkedConvert('title', (v) => v as String?),
        significant: $checkedConvert(
          'significant',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        volatile: $checkedConvert(
          'volatile',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        observedAtMode: $checkedConvert(
          'observedAtMode',
          (v) => $enumDecodeNullable(
            _$EdpStreamFieldsObservedAtModeEnumEnumMap,
            v,
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpStreamFieldsToJson(EdpStreamFields instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'significant': ?instance.significant,
      'volatile': ?instance.volatile,
      'observedAtMode':
          ?_$EdpStreamFieldsObservedAtModeEnumEnumMap[instance.observedAtMode],
    };

const _$EdpStreamFieldsObservedAtModeEnumEnumMap = {
  EdpStreamFieldsObservedAtModeEnum.upstream: 'upstream',
  EdpStreamFieldsObservedAtModeEnum.received: 'received',
};
