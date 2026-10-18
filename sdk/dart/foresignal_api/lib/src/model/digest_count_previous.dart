//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_count_previous.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestCountPrevious {
  /// Returns a new [DigestCountPrevious] instance.
  DigestCountPrevious({

     this.new_,

     this.changed,

     this.removed,

     this.active,
  });

  @JsonKey(
    
    name: r'new',
    required: false,
    includeIfNull: false,
  )


  final int? new_;



  @JsonKey(
    
    name: r'changed',
    required: false,
    includeIfNull: false,
  )


  final int? changed;



  @JsonKey(
    
    name: r'removed',
    required: false,
    includeIfNull: false,
  )


  final int? removed;



      /// null until 038's rollups (record_live keeps only the latest receipt)
  @JsonKey(
    
    name: r'active',
    required: false,
    includeIfNull: false,
  )


  final int? active;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestCountPrevious &&
      other.new_ == new_ &&
      other.changed == changed &&
      other.removed == removed &&
      other.active == active;

    @override
    int get hashCode =>
        new_.hashCode +
        changed.hashCode +
        removed.hashCode +
        active.hashCode;

  factory DigestCountPrevious.fromJson(Map<String, dynamic> json) => _$DigestCountPreviousFromJson(json);

  Map<String, dynamic> toJson() => _$DigestCountPreviousToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

