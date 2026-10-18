// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'originator.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OriginatorCWProxy {
  Originator id(String id);

  Originator class_(OriginatorClass_Enum class_);

  Originator independenceGroup(String? independenceGroup);

  Originator aggregates(List<String>? aggregates);

  Originator notes(String? notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Originator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Originator(...).copyWith(id: 12, name: "My name")
  /// ```
  Originator call({
    String id,
    OriginatorClass_Enum class_,
    String? independenceGroup,
    List<String>? aggregates,
    String? notes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOriginator.copyWith(...)` or call `instanceOfOriginator.copyWith.fieldName(value)` for a single field.
class _$OriginatorCWProxyImpl implements _$OriginatorCWProxy {
  const _$OriginatorCWProxyImpl(this._value);

  final Originator _value;

  @override
  Originator id(String id) => call(id: id);

  @override
  Originator class_(OriginatorClass_Enum class_) => call(class_: class_);

  @override
  Originator independenceGroup(String? independenceGroup) =>
      call(independenceGroup: independenceGroup);

  @override
  Originator aggregates(List<String>? aggregates) =>
      call(aggregates: aggregates);

  @override
  Originator notes(String? notes) => call(notes: notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Originator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Originator(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Originator call({
    Object? id = const $CopyWithPlaceholder(),
    Object? class_ = const $CopyWithPlaceholder(),
    Object? independenceGroup = const $CopyWithPlaceholder(),
    Object? aggregates = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
  }) {
    return Originator(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      class_: class_ == const $CopyWithPlaceholder() || class_ == null
          ? _value.class_
          // ignore: cast_nullable_to_non_nullable
          : class_ as OriginatorClass_Enum,
      independenceGroup: independenceGroup == const $CopyWithPlaceholder()
          ? _value.independenceGroup
          // ignore: cast_nullable_to_non_nullable
          : independenceGroup as String?,
      aggregates: aggregates == const $CopyWithPlaceholder()
          ? _value.aggregates
          // ignore: cast_nullable_to_non_nullable
          : aggregates as List<String>?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
    );
  }
}

extension $OriginatorCopyWith on Originator {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOriginator.copyWith(...)` or `instanceOfOriginator.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OriginatorCWProxy get copyWith => _$OriginatorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Originator _$OriginatorFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Originator', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'class']);
      final val = Originator(
        id: $checkedConvert('id', (v) => v as String),
        class_: $checkedConvert(
          'class',
          (v) => $enumDecode(_$OriginatorClass_EnumEnumMap, v),
        ),
        independenceGroup: $checkedConvert(
          'independenceGroup',
          (v) => v as String?,
        ),
        aggregates: $checkedConvert(
          'aggregates',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        notes: $checkedConvert('notes', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'class_': 'class'});

Map<String, dynamic> _$OriginatorToJson(Originator instance) =>
    <String, dynamic>{
      'id': instance.id,
      'class': _$OriginatorClass_EnumEnumMap[instance.class_]!,
      'independenceGroup': ?instance.independenceGroup,
      'aggregates': ?instance.aggregates,
      'notes': ?instance.notes,
    };

const _$OriginatorClass_EnumEnumMap = {
  OriginatorClass_Enum.sensorNetwork: 'sensor-network',
  OriginatorClass_Enum.agency: 'agency',
  OriginatorClass_Enum.aggregator: 'aggregator',
  OriginatorClass_Enum.media: 'media',
  OriginatorClass_Enum.crowd: 'crowd',
  OriginatorClass_Enum.operator_: 'operator',
  OriginatorClass_Enum.derived: 'derived',
  OriginatorClass_Enum.social: 'social',
};
