// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_source_info_originator.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSourceInfoOriginatorCWProxy {
  EdpSourceInfoOriginator id(String id);

  EdpSourceInfoOriginator class_(EdpSourceInfoOriginatorClass_Enum class_);

  EdpSourceInfoOriginator aggregates(List<String>? aggregates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoOriginator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoOriginator(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSourceInfoOriginator call({
    String id,
    EdpSourceInfoOriginatorClass_Enum class_,
    List<String>? aggregates,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSourceInfoOriginator.copyWith(...)` or call `instanceOfEdpSourceInfoOriginator.copyWith.fieldName(value)` for a single field.
class _$EdpSourceInfoOriginatorCWProxyImpl
    implements _$EdpSourceInfoOriginatorCWProxy {
  const _$EdpSourceInfoOriginatorCWProxyImpl(this._value);

  final EdpSourceInfoOriginator _value;

  @override
  EdpSourceInfoOriginator id(String id) => call(id: id);

  @override
  EdpSourceInfoOriginator class_(EdpSourceInfoOriginatorClass_Enum class_) =>
      call(class_: class_);

  @override
  EdpSourceInfoOriginator aggregates(List<String>? aggregates) =>
      call(aggregates: aggregates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoOriginator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoOriginator(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSourceInfoOriginator call({
    Object? id = const $CopyWithPlaceholder(),
    Object? class_ = const $CopyWithPlaceholder(),
    Object? aggregates = const $CopyWithPlaceholder(),
  }) {
    return EdpSourceInfoOriginator(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      class_: class_ == const $CopyWithPlaceholder() || class_ == null
          ? _value.class_
          // ignore: cast_nullable_to_non_nullable
          : class_ as EdpSourceInfoOriginatorClass_Enum,
      aggregates: aggregates == const $CopyWithPlaceholder()
          ? _value.aggregates
          // ignore: cast_nullable_to_non_nullable
          : aggregates as List<String>?,
    );
  }
}

extension $EdpSourceInfoOriginatorCopyWith on EdpSourceInfoOriginator {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSourceInfoOriginator.copyWith(...)` or `instanceOfEdpSourceInfoOriginator.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSourceInfoOriginatorCWProxy get copyWith =>
      _$EdpSourceInfoOriginatorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSourceInfoOriginator _$EdpSourceInfoOriginatorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpSourceInfoOriginator', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'class']);
  final val = EdpSourceInfoOriginator(
    id: $checkedConvert('id', (v) => v as String),
    class_: $checkedConvert(
      'class',
      (v) => $enumDecode(_$EdpSourceInfoOriginatorClass_EnumEnumMap, v),
    ),
    aggregates: $checkedConvert(
      'aggregates',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'class_': 'class'});

Map<String, dynamic> _$EdpSourceInfoOriginatorToJson(
  EdpSourceInfoOriginator instance,
) => <String, dynamic>{
  'id': instance.id,
  'class': _$EdpSourceInfoOriginatorClass_EnumEnumMap[instance.class_]!,
  'aggregates': ?instance.aggregates,
};

const _$EdpSourceInfoOriginatorClass_EnumEnumMap = {
  EdpSourceInfoOriginatorClass_Enum.sensorNetwork: 'sensor-network',
  EdpSourceInfoOriginatorClass_Enum.agency: 'agency',
  EdpSourceInfoOriginatorClass_Enum.aggregator: 'aggregator',
  EdpSourceInfoOriginatorClass_Enum.media: 'media',
  EdpSourceInfoOriginatorClass_Enum.crowd: 'crowd',
  EdpSourceInfoOriginatorClass_Enum.social: 'social',
  EdpSourceInfoOriginatorClass_Enum.operator_: 'operator',
  EdpSourceInfoOriginatorClass_Enum.derived: 'derived',
};
