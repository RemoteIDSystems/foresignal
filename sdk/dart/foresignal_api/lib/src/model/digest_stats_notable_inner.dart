//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/digest_notable_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_stats_notable_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestStatsNotableInner {
  /// Returns a new [DigestStatsNotableInner] instance.
  DigestStatsNotableInner({

    required  this.kind,

    required  this.items,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<DigestNotableItem> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestStatsNotableInner &&
      other.kind == kind &&
      other.items == items;

    @override
    int get hashCode =>
        kind.hashCode +
        items.hashCode;

  factory DigestStatsNotableInner.fromJson(Map<String, dynamic> json) => _$DigestStatsNotableInnerFromJson(json);

  Map<String, dynamic> toJson() => _$DigestStatsNotableInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

